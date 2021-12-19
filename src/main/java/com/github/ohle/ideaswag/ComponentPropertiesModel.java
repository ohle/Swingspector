package com.github.ohle.ideaswag;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.eudaemon.swag.ComponentProperty;

public class ComponentPropertiesModel extends AbstractTableModel {

    private final List<ComponentProperty> properties;

    public ComponentPropertiesModel(List<ComponentProperty> properties_) {
        properties = properties_;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Property";
        } else if (column == 1) {
            return "Value";
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return properties.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return properties.get(rowIndex).key;
        } else {
            return properties.get(rowIndex).valueDescription;
        }
    }
}
