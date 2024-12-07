import os
import subprocess
import tkinter as tk
from tkinter import filedialog, messagebox
import pandas as pd
import re

def run_commands():
    # Get values from the UI
    xml_file = file_var.get()
    seed = seed_var.get()
    maxload = maxload_var.get()
    minload = minload_var.get()
    step = step_var.get()

    # Check if inputs are valid
    if not xml_file or not os.path.isfile(xml_file):
        messagebox.showerror("Error", "Please select a valid XML file.")
        return
    if not (seed.isdigit() and 1 <= int(seed) <= 25):
        messagebox.showerror("Error", "Seed must be a positive integer between 1 and 25.")
        return
    if not (maxload.isdigit() and int(maxload) > 0):
        messagebox.showerror("Error", "Maxload must be a positive integer.")
        return
    if not (minload.isdigit() and int(minload) > 0):
        messagebox.showerror("Error", "Minload must be a positive integer.")
        return
    if not (step.isdigit() and int(step) > 0):
        messagebox.showerror("Error", "Step must be a positive integer.")
        return
    if int(maxload) <= int(minload):
        messagebox.showerror("Error", "Maxload must be greater than Minload.")
        return

    # Show loading label
    loading_label.config(text="Loading...")
    root.update_idletasks()  # Update the UI to show "Loading"

    # Prepare the commands
    commands = [
        f"java -jar t0.jar {xml_file} {seed} -trace {minload} {maxload} {step} > res1.txt",
        f"java -jar t0.jar {xml_file} {seed} -verbose {minload} {maxload} {step} > res2.txt"
    ]

    # Execute the commands one by one
    for cmd in commands:
        try:
            subprocess.run(cmd, shell=True, check=True)
        except subprocess.CalledProcessError as e:
            loading_label.config(text="")  # Hide loading label on error
            messagebox.showerror("Error", f"Command failed: {cmd}\n{e}")
            return

    # Convert the trace file to Excel after commands are executed
    stepval = int(step)
    maxloadval = int(maxload)
    loadval = int(minload)

    while loadval <= maxloadval:
        xml_file_base = os.path.splitext(xml_file)[0]
        load = str(loadval)
        convert_trace_to_excel(f"{xml_file_base}_Load_{load}.0.txt", f"{xml_file_base}_output_trace{load}.xlsx")
        loadval += stepval

    # Hide loading label after completion
    loading_label.config(text="")

def convert_trace_to_excel(trace_file_path, output_file):
    flows_data = []
    lightpaths_data = []

    # Parse the trace file
    if not os.path.isfile(trace_file_path):
        messagebox.showerror("Error", f"Trace file not found: {trace_file_path}")
        return

    with open(trace_file_path, 'r') as file:
        for line in file:
            parts = line.strip().split()
            
            # Parse "Flows" events
            if line.startswith("flow-accepted") or line.startswith("flow-blocked"):
                event = parts[0]
                time = parts[1] if parts[1] != "-" else "-"
                flow_id, src, dst, rate, duration, cos = parts[2:8]
                lightpaths = " ".join(parts[8:]) if len(parts) > 8 else "-"
                flows_data.append([event, time, flow_id, src, dst, rate, duration, cos, lightpaths])

            elif line.startswith("flow-arrived") or line.startswith("flow-departed"):
                event = parts[0]
                time = parts[1] if parts[1] != "-" else "-"
                flow_id = parts[2]
                src = parts[3] if parts[3] != "-" else "-"
                dst = parts[4] if parts[4] != "-" else "-"
                rate = parts[5] if parts[5] != "-" else "-"
                duration = parts[6] if parts[6] != "-" else "-"
                cos = parts[7] if len(parts) > 7 else "-"
                lightpaths = parts[8] if len(parts) > 8 else "-"
                flows_data.append([event, time, flow_id, src, dst, rate, duration, cos, lightpaths])

            # Parse "Lightpaths" events
            elif line.startswith("lightpath-created") or line.startswith("lightpath-removed"):
                event = parts[0]
                lp_id = parts[1]
                src = parts[2]
                dst = parts[3]
                link_wavelength = " ".join(parts[4:]).replace('_', ' ') if len(parts) > 4 else "-"
                lightpaths_data.append([event, lp_id, src, dst, link_wavelength])

    # Convert parsed data to dataframes
    flows_df = pd.DataFrame(flows_data, columns=["Event", "Time", "ID", "Src", "Dst", "Rate", "Duration", "CoS", "Lightpaths"])
    lightpaths_df = pd.DataFrame(lightpaths_data, columns=["Event", "ID", "Src", "Dst", "Link_Wavelength"])

    # Save data to Excel
    with pd.ExcelWriter(output_file) as writer:
        flows_df.to_excel(writer, sheet_name="Flows", index=False)
        lightpaths_df.to_excel(writer, sheet_name="Lightpaths", index=False)

    # Delete the trace file
    try:
        os.remove(trace_file_path)
    except OSError as e:
        messagebox.showerror("Error", f"Failed to delete trace file: {trace_file_path}\n{e}")

def choose_file():
    file_path = filedialog.askopenfilename(
        initialdir=os.getcwd(), title="Select XML File",
        filetypes=(("XML files", "*.xml"), ("All files", "*.*"))
    )
    file_var.set(file_path)

# Create the main window
root = tk.Tk()
root.title("Run WDMSim Commands")

# Variables for storing user input
file_var = tk.StringVar()
seed_var = tk.StringVar()
maxload_var = tk.StringVar()
minload_var = tk.StringVar()
step_var = tk.StringVar()

# UI layout
tk.Label(root, text="Select XML File:").grid(row=0, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=file_var, width=50).grid(row=0, column=1, padx=10, pady=5)
tk.Button(root, text="Browse", command=choose_file).grid(row=0, column=2, padx=10, pady=5)

tk.Label(root, text="Seed (1-25):").grid(row=1, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=seed_var).grid(row=1, column=1, padx=10, pady=5)

tk.Label(root, text="Maxload:").grid(row=2, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=maxload_var).grid(row=2, column=1, padx=10, pady=5)

tk.Label(root, text="Minload:").grid(row=3, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=minload_var).grid(row=3, column=1, padx=10, pady=5)

tk.Label(root, text="Step:").grid(row=4, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=step_var).grid(row=4, column=1, padx=10, pady=5)

loading_label = tk.Label(root, text="", fg="blue")
loading_label.grid(row=5, column=1, pady=10)  # Position the loading label

tk.Button(root, text="Run Commands", command=run_commands).grid(row=6, column=0, columnspan=3, pady=20)

# Start the main loop
root.mainloop()
