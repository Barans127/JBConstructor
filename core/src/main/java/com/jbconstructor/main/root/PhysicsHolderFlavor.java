package com.jbconstructor.main.root;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class PhysicsHolderFlavor implements Transferable {

    private DataFlavor flavor = new DataFlavor(PhysicsHolder.class, "PhysicsHolder");
    private PhysicsHolder holder;

    /** Set physics holder before using this flavor. */
    public void setPhysicsHolder(PhysicsHolder e){
        holder = e;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return flavor.equals(dataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, RuntimeException {
        if (isDataFlavorSupported(dataFlavor)){
            if (holder == null){
                throw new RuntimeException("Physics holder not set.");
            }
            return holder;
        }else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
