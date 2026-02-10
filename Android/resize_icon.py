import cv2
import numpy as np
import os

def resize_and_pad(input_path, output_path, padding_percent=0.35):
    # Read the image
    img = cv2.imread(input_path, cv2.IMREAD_UNCHANGED)
    
    if img is None:
        print(f"Error: Could not read image at {input_path}")
        return

    # Get dimensions
    h, w = img.shape[:2]
    
    # Calculate new size (keeping aspect ratio)
    scale_factor = 1.0 - (padding_percent * 2)
    new_w = int(w * scale_factor)
    new_h = int(h * scale_factor)
    
    # Resize the original image
    resized_img = cv2.resize(img, (new_w, new_h), interpolation=cv2.INTER_AREA)
    
    # Create a blank canvas of the original size
    # If original has alpha channel, make canvas transparent. If not, make it black (as user wants black)
    if img.shape[2] == 4:
        canvas = np.zeros((h, w, 4), dtype=np.uint8)
    else:
        canvas = np.zeros((h, w, 3), dtype=np.uint8) # Black canvas
        
    # Calculate position to center
    x_offset = (w - new_w) // 2
    y_offset = (h - new_h) // 2
    
    # Paste resized image onto canvas
    canvas[y_offset:y_offset+new_h, x_offset:x_offset+new_w] = resized_img
    
    # Save
    cv2.imwrite(output_path, canvas)
    print(f"Successfully resized and padded image. Saved to {output_path}")

if __name__ == "__main__":
    input_file = "/home/katchy/Documentos/AntiProcast/app/src/main/res/drawable/ic_app_logo.png"
    resize_and_pad(input_file, input_file)
