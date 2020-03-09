import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;

public class Client extends JFrame {
    JPanel login_container;
    JPanel app_container;
    JPanel chat_container;
    JTabbedPane nav_container;
    JPanel chat_area;
    JPanel message_container;

    JTextField txtLoginName;
    JTextField txtMessage;
    CardLayout cardLayout;
    JList activeList;
    JList conversionList;

    JButton btnLogIn;
    JButton btnSend;

    Socket s;
    DataInputStream dis;
    DataOutputStream dos;

    DefaultListModel model;

    HashMap<String, DefaultListModel> messages = new HashMap<>();


    public Client() {
        initComponents();
        btnLogIn.addActionListener(e -> {
            try {
                s = new Socket("192.168.1.9", 1402);

                dis = new DataInputStream(s.getInputStream());
                dos = new DataOutputStream(s.getOutputStream());

                String userName = txtLoginName.getText();
                dos.writeUTF("2#" + userName);

                String reply = dis.readUTF();
                if (reply.equals("0")) {
                    JOptionPane.showMessageDialog(app_container, "This username is taken.");
                    dis.close();
                    dos.close();
                    s.close();
                    return;
                }
                String[] tmp = reply.split("#");
                activeList.setListData(Arrays.copyOfRange(tmp, 1, tmp.length));
                cardLayout.show(app_container, "chat");
                JOptionPane.showMessageDialog(app_container, "Hello there " + userName);

                Thread readMessage = new Thread(() -> {
                    while (true) {
                        try {
                            String msg = dis.readUTF();
                            String[] received = msg.split("#");
                            System.out.println(msg);
                            switch (received[0]) {
                                case "3":
                                    activeList.setListData(Arrays.copyOfRange(received, 1, received.length));
                                    break;
                                case "5":
                                    String sender = received[1];
                                    if (!messages.containsKey(sender)) {
                                        model.addElement(sender);
                                        DefaultListModel msgModel = new DefaultListModel();
                                        generateNewConversion(msgModel, sender);
                                        messages.put(sender, msgModel);
                                    }
                                    messages.get(sender).addElement(new Message(received[2], false, LocalDate.now()));
                                    break;
                            }
                            Thread.sleep(5000);
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                });
                readMessage.start();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

        activeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String name = (String) activeList.getSelectedValue();
                if (!messages.containsKey(name)) {
                    model.addElement(name);
                    DefaultListModel msgModel = new DefaultListModel();
                    generateNewConversion(msgModel, name);
                    messages.put(name, msgModel);
                    cardLayout.show(message_container, name);
                    nav_container.setSelectedIndex(1);
                    conversionList.setSelectedValue(name, true);
                } else {
                    nav_container.setSelectedIndex(1);
                    conversionList.setSelectedValue(name, true);
                }
            }
        });

        conversionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String name = (String) conversionList.getSelectedValue();
                cardLayout.show(message_container, name);
            }
        });

        btnSend.addActionListener(actionEvent -> {
            String content = txtMessage.getText();
            String recipient = (String) conversionList.getSelectedValue();
            try {
                dos.writeUTF(recipient + "#" + content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            messages.get(recipient).addElement(new Message(content, true, LocalDate.now()));
            txtMessage.setText("");
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dos.writeUTF("logout");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        Client c = new Client();
        c.setSize(800, 600);
        c.setDefaultCloseOperation(EXIT_ON_CLOSE);
        c.setTitle("Chat Application");
        c.setResizable(false);
        c.setVisible(true);

    }

    void generateNewConversion(DefaultListModel model, String userName) {
        JList conversion = new JList(model);
        JPanel panel = new JPanel();
        JLabel user = new JLabel(userName);
        JPanel name_container = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();

        name_container.setLayout(new FlowLayout(FlowLayout.LEFT));
        Font font = new Font("SansSerif", Font.PLAIN, 20);
        user.setFont(font);
        user.setBorder(new EmptyBorder(0, 10, 10, 10));
        name_container.add(user);

        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(400, 480));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 5;
        gbc.ipady = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        name_container.setBackground(Color.white);
        panel.add(name_container, gbc);
        gbc.gridy = 1;
        gbc.ipady = 420;
        panel.add(new JScrollPane(conversion), gbc);
        panel.setBackground(Color.white);


        conversion.setCellRenderer(new CustomListRender() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
                setPreferredSize(new Dimension(100, 40));
                setBorder(new EmptyBorder(5, 5, 5, 5));
                content.setText(((Message) o).getContent());
                setLayout(new FlowLayout(FlowLayout.LEFT));
                if (((Message) o).isSender()) {
                    setLayout(new FlowLayout(FlowLayout.RIGHT));
                }
                return this;
            }
        });
        message_container.add(userName, panel);
    }

    void initComponents() {
        app_container = new JPanel();
        cardLayout = new CardLayout();
        app_container.setLayout(cardLayout);

        // Login view
        login_container = new JPanel();
        login_container.setLayout(new GridLayout(3, 1));
        login_container.setBorder(new EmptyBorder(200, 20, 200, 20));
        JLabel welcome = new JLabel("Chat With Jack");
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        Font font = new Font("SansSerif", Font.PLAIN, 30);
        welcome.setFont(font);
        login_container.add(welcome);
        txtLoginName = new JTextField();
        txtLoginName.setPreferredSize(new Dimension(400, 30));
        btnLogIn = new JButton("Let's go");
        JPanel container = new JPanel();
        container.add(txtLoginName);
        container.add(btnLogIn);
        login_container.add(container);


        // Navigation Container
        nav_container = new JTabbedPane();
        nav_container.setBackground(Color.white);
        activeList = new JList();
        activeList.setCellRenderer(new CustomListRender());
        model = new DefaultListModel();
        conversionList = new JList(model);
        conversionList.setCellRenderer(new CustomListRender() {
            @Override
            public Component getListCellRendererComponent(JList<?> jList, Object o, int i, boolean b, boolean b1) {
                super.getListCellRendererComponent(jList, o, i, b, b1);
                if (b) {
                    setBackground(jList.getSelectionBackground());
                    content.setForeground(Color.white);
                } else {
                    setBackground(jList.getBackground());
                    content.setForeground(Color.black);
                }
                return this;
            }
        });
        JScrollPane active_container = new JScrollPane(activeList);
        JScrollPane conversion_container = new JScrollPane(conversionList);
        nav_container.add("Active", active_container);
        nav_container.add("Conversion", conversion_container);

        // Chat view
        chat_container = new JPanel();
        chat_container.setLayout(new GridLayout());
        chat_container.add(nav_container);
        chat_area = new JPanel();
        chat_area.setBackground(Color.white);


        // Chat Area
        chat_container.add(chat_area);
        chat_container.setBackground(Color.white);


        JPanel input_container = new JPanel();
        input_container.setBackground(Color.white);
        txtMessage = new JTextField();
        btnSend = new JButton("Send");
        btnSend.setPreferredSize(new Dimension(80, 40));
        txtMessage.setPreferredSize(new Dimension(300, 40));
        input_container.add(txtMessage);
        input_container.add(btnSend);
        input_container.setPreferredSize(new Dimension(400, 50));
        message_container = new JPanel();

        message_container.setPreferredSize(new Dimension(400, 480));
        message_container.setBackground(Color.white);
        message_container.setLayout(cardLayout);
        chat_area.add(message_container);
        chat_area.add(input_container);
        chat_area.setBorder(new EmptyBorder(20, 0, 0, 0));


        app_container.add("login", login_container);
        app_container.add("chat", chat_container);

        Container c = getContentPane();
        c.add(app_container);


    }
}
