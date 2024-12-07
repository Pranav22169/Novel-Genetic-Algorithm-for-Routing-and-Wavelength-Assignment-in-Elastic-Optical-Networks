import os
import subprocess
import tkinter as tk
from tkinter import filedialog, messagebox
import pandas as pd
import random
import re
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
def run_commands():
    # Get values from the UI
    xml_file = file_var.get()
    maxload = maxload_var.get()
    minload = minload_var.get()
    step = step_var.get()

    # Validate inputs
    if not xml_file or not os.path.isfile(xml_file):
        messagebox.showerror("Error", "Please select a valid XML file.")
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
    root.update_idletasks()

    # Generate random seed between 1 and 5
    seed = random.randint(1, 5)

    # Prepare the command
    xml_file_base = os.path.splitext(xml_file)[0]
    output_file = f"{xml_file_base}.txt"
    command = f"java -jar t0.jar {xml_file} {seed} -trace {minload} {maxload} {step} > {output_file}"

    # Execute the command
    try:
        subprocess.run(command, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        loading_label.config(text="")
        messagebox.showerror("Error", f"Command failed: {command}\n{e}")
        return

    # Hide loading label after completion
    loading_label.config(text="")

    # Plot results using the generated file
    plot_data()

    # Convert the trace file to Excel
    stepval = int(step)
    maxloadval = int(maxload)
    loadval = int(minload)

    while loadval <= maxloadval:
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

def plot_data():
    # Get the file name base 
    xml_file_base = os.path.splitext(os.path.basename(file_var.get()))[0]
    result_file_path = f"{xml_file_base}.txt"
    
    # Lists to store parsed data
    loads = []
    total_blocked = []
    blocked_bandwidth = []
    required_bandwidth = []

    # Parse the generated result file to get load and bandwidth metrics
    if not os.path.isfile(result_file_path):
        messagebox.showerror("Error", f"Result file not found: {result_file_path}")
        return

    with open(result_file_path, 'r') as file:
        for line in file:
            load_match = re.search(r'Load:(\d+.\d+)', line)
            total_blocked_match = re.search(r'Total Blocked: \d+ \((\d+.\d+)%\)', line)
            blocked_bandwidth_match = re.search(r'Blocked Bandwidth: (\d+)', line)
            required_bandwidth_match = re.search(r'Required Bandwidth: (\d+)', line)

            if load_match:
                loads.append(float(load_match.group(1)))
            if total_blocked_match:
                total_blocked.append(float(total_blocked_match.group(1)))  # Extract percentage
            if blocked_bandwidth_match:
                blocked_bandwidth.append(int(blocked_bandwidth_match.group(1)))
            if required_bandwidth_match:
                required_bandwidth.append(int(required_bandwidth_match.group(1)))

    # Calculate Available Bandwidth (Required - Blocked)
    available_bandwidth = [req - blk for req, blk in zip(required_bandwidth, blocked_bandwidth)]

    # Create a new window for plots
    plot_window = tk.Toplevel(root)
    plot_window.title("Load vs Bandwidth Visualizations")

    # Create the Total Blocked plot (percentage vs Load)
    fig1, ax1 = plt.subplots(figsize=(5, 4))
    ax1.plot(loads, total_blocked, marker='o', color='b', label='Total Blocked (%)')
    ax1.set_xlabel('Load')
    ax1.set_ylabel('Total Blocked (%)')
    ax1.set_title('Total Blocked vs. Load')
    ax1.legend()
    ax1.grid(True)

    # Embed Total Blocked plot in Tkinter
    canvas1 = FigureCanvasTkAgg(fig1, plot_window)
    canvas1.get_tk_widget().pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
    canvas1.draw()

    # Create the Available Bandwidth plot
    fig2, ax2 = plt.subplots(figsize=(5, 4))
    ax2.plot(loads, available_bandwidth, marker='o', color='g', label='Available Bandwidth')
    ax2.set_xlabel('Load')
    ax2.set_ylabel('Available Bandwidth')
    ax2.set_title('Available Bandwidth vs. Load')
    ax2.legend()
    ax2.grid(True)

    # Embed Available Bandwidth plot in Tkinter
    canvas2 = FigureCanvasTkAgg(fig2, plot_window)
    canvas2.get_tk_widget().pack(side=tk.RIGHT, fill=tk.BOTH, expand=True)
    canvas2.draw()

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
maxload_var = tk.StringVar()
minload_var = tk.StringVar()
step_var = tk.StringVar()

# UI layout
tk.Label(root, text="Select XML File:").grid(row=0, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=file_var, width=50).grid(row=0, column=1, padx=10, pady=5)
tk.Button(root, text="Browse", command=choose_file).grid(row=0, column=2, padx=10, pady=5)

tk.Label(root, text="Maxload:").grid(row=1, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=maxload_var).grid(row=1, column=1, padx=10, pady=5)

tk.Label(root, text="Minload:").grid(row=2, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=minload_var).grid(row=2, column=1, padx=10, pady=5)

tk.Label(root, text="Step:").grid(row=3, column=0, padx=10, pady=5)
tk.Entry(root, textvariable=step_var).grid(row=3, column=1, padx=10, pady=5)

loading_label = tk.Label(root, text="", fg="blue")
loading_label.grid(row=4, column=1, pady=10)

tk.Button(root, text="Run Commands", command=run_commands).grid(row=5, column=0, columnspan=3, pady=20)

# Start the main loop
root.mainloop()
