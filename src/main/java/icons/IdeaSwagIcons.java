package icons;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.util.IconLoader;

import de.eudaemon.swag.ComponentDescription;

import javax.swing.Icon;

public interface IdeaSwagIcons {
    Icon Button = IconLoader.getIcon("/icons/button.svg", IdeaSwagIcons.class);
    Icon ButtonGroup = IconLoader.getIcon("/icons/buttonGroup.svg", IdeaSwagIcons.class);
    Icon Checkbox = IconLoader.getIcon("/icons/checkbox.svg", IdeaSwagIcons.class);
    Icon Combobox = IconLoader.getIcon("/icons/combobox.svg", IdeaSwagIcons.class);
    Icon EditorPane = IconLoader.getIcon("/icons/editorPane.svg", IdeaSwagIcons.class);
    Icon FormattedTextField =
            IconLoader.getIcon("/icons/formattedTextField.svg", IdeaSwagIcons.class);
    Icon Label = IconLoader.getIcon("/icons/label.svg", IdeaSwagIcons.class);
    Icon List = IconLoader.getIcon("/icons/list.svg", IdeaSwagIcons.class);
    Icon Panel = IconLoader.getIcon("/icons/panel.svg", IdeaSwagIcons.class);
    Icon ProgressBar = IconLoader.getIcon("/icons/progressbar.svg", IdeaSwagIcons.class);
    Icon RadioButton = IconLoader.getIcon("/icons/radioButton.svg", IdeaSwagIcons.class);
    Icon Scrollbar = IconLoader.getIcon("/icons/scrollbar.svg", IdeaSwagIcons.class);
    Icon ScrollPane = IconLoader.getIcon("/icons/scrollPane.svg", IdeaSwagIcons.class);
    Icon Separator = IconLoader.getIcon("/icons/separator.svg", IdeaSwagIcons.class);
    Icon Slider = IconLoader.getIcon("/icons/slider.svg", IdeaSwagIcons.class);
    Icon Spinner = IconLoader.getIcon("/icons/spinner.svg", IdeaSwagIcons.class);
    Icon SplitPane = IconLoader.getIcon("/icons/splitPane.svg", IdeaSwagIcons.class);
    Icon TabbedPane = IconLoader.getIcon("/icons/tabbedPane.svg", IdeaSwagIcons.class);
    Icon Table = IconLoader.getIcon("/icons/table.svg", IdeaSwagIcons.class);
    Icon TextArea = IconLoader.getIcon("/icons/textArea.svg", IdeaSwagIcons.class);
    Icon TextField = IconLoader.getIcon("/icons/textField.svg", IdeaSwagIcons.class);
    Icon TextPane = IconLoader.getIcon("/icons/textPane.svg", IdeaSwagIcons.class);
    Icon ToolbarSeparator = IconLoader.getIcon("/icons/toolbarSeparator.svg", IdeaSwagIcons.class);
    Icon Toolbar = IconLoader.getIcon("/icons/toolbar.svg", IdeaSwagIcons.class);
    Icon Tree = IconLoader.getIcon("/icons/tree.svg", IdeaSwagIcons.class);
    Icon Unknown = IconLoader.getIcon("/icons/unknown.svg", IdeaSwagIcons.class);
    Icon Window = Actions.MoveToWindow;
    Icon Width = IconLoader.getIcon("/icons/width.svg", IdeaSwagIcons.class);
    Icon Height = IconLoader.getIcon("/icons/height.svg", IdeaSwagIcons.class);

    static Icon fromDescription(ComponentDescription description) {
        switch (description.iconKey) {
            case "button":
                return Button;
            case "checkbox":
                return Checkbox;
            case "combobox":
                return Combobox;
            case "editorPane":
                return EditorPane;
            case "formattedTextField":
                return FormattedTextField;
            case "label":
                return Label;
            case "list":
                return List;
            case "panel":
                return Panel;
            case "progressbar":
                return ProgressBar;
            case "radioButton":
                return RadioButton;
            case "scrollbar":
                return Scrollbar;
            case "scrollPane":
                return ScrollPane;
            case "separator":
                return Separator;
            case "slider":
                return Slider;
            case "spinner":
                return Spinner;
            case "splitPane":
                return SplitPane;
            case "tabbedPane":
                return TabbedPane;
            case "table":
                return Table;
            case "textArea":
                return TextArea;
            case "textField":
                return TextField;
            case "textPane":
                return TextPane;
            case "toolbarSeparator":
                return ToolbarSeparator;
            case "toolbar":
                return Toolbar;
            case "tree":
                return Tree;
            case "window":
                return Window;
            default:
                return Unknown;
        }
    }
}
