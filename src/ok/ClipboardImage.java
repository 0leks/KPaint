package ok;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;

public class ClipboardImage implements Transferable {
	private Image image;

	public ClipboardImage(Image image) {
		this.image = image;
	}

	// Returns supported flavors
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	// Returns true if flavor is supported
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	// Returns image
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!DataFlavor.imageFlavor.equals(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return image;
	}
	
	public static void setClipboard(Image image) {
		ClipboardImage imgSel = new ClipboardImage(image);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
	}
}
