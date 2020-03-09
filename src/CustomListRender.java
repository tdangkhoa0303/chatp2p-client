import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CustomListRender extends JPanel implements ListCellRenderer<Object> {
    JLabel content = new JLabel();

    public CustomListRender() {
        add(content);
        setBackground(Color.white);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
        setPreferredSize(new Dimension(100, 40));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        content.setText((String) o);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        return this;
    }
}
