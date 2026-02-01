import os

def dump_code_to_notepad(base_path, output_file):
    with open(output_file, "w", encoding="utf-8") as out_file:
        for root, dirs, files in os.walk(base_path):
            # Check if we're inside a direct subfolder of the base path
            if root == base_path:
                continue  # Skip the base folder itself

            # Extract folder name (e.g., dto, entity, service)
            folder_name = os.path.basename(root)
            out_file.write("\n" + "="*40 + "\n")
            out_file.write(f"DIRECTORY: {folder_name}\n")
            out_file.write("="*40 + "\n\n")

            for file in files:
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        out_file.write(f"\n--- FILE: {file} ---\n\n")
                        out_file.write(f.read())
                        out_file.write("\n\n")
                except Exception as e:
                    out_file.write(f"\n[Error reading {file}: {e}]\n\n")

    print(f"All files dumped into {output_file}")


if __name__ == "__main__":
    #base_path = r"C:\Users\kashi\OneDrive\Desktop\Full_StackProjects\ProjectClinicManagementSystem\Backend\clinicapp\clinicapp\src\main\java\com\example\clinicapp"
    base_path = r"C:\Users\kashi\OneDrive\Desktop\Full_StackProjects\ProjectClinicManagementSystem\Backend\clinicapp\clinicapp\src\main\java\com\example\clinicapp"
    
    output_file = r"C:\Users\kashi\OneDrive\Desktop\26OCT.txt"
    dump_code_to_notepad(base_path, output_file)
