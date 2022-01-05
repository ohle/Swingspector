package de.eudaemon.ideaswag;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import de.eudaemon.swag.ComponentProperty;
import de.eudaemon.swag.ComponentProperty.ListenerSet;

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
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 1) {
            return ComponentProperty.class;
        } else {
            return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 && properties.get(rowIndex) instanceof ListenerSet;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ComponentProperty prop = properties.get(rowIndex);
        if (columnIndex == 0) {
            return prop.key;
        } else {
            return prop;
        }
    }
}
