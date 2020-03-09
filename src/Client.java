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
    JPanel message_container;

    JTextField txtLoginName;
    CardLayout cardLayout;
    JList activeList;
    JList conversionList;

    JButton btnLogIn;

    Socket s;
    DataInputStream dis;
    DataOutputStream dos;

    DefaultListModel model;
    boolean isLogin = false;

    HashMap<String, DefaultListModel> messages = new HashMap<>();


    public Client() {
        initComponents();
        btnLogIn.addActionListener(e -> {
            try {
                s = new Socket("localhost", 1402);

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
                isLogin = true;
                JOptionPane.showMessageDialog(app_container, "Hello there " + userName);

                Thread readMessage = new Thread(() -> {
                    while (true) {
                        try {
                            String msg = dis.readUTF();
                            String[] received = msg.split("#");
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
                            Thread.sleep(1000);
                        } catch (IOException | InterruptedException ex) {
                            ex.getMessage();
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isLogin) {
                    try {
                        dos.writeUTF("logout");
                        dis.close();
                        s.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });
        addWindowListener(new WindowAdapter() {
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
        JPanel input_container = new JPanel();
        GridBagConstraints gbc = new GridBagConstraints();

        name_container.setLayout(new FlowLayout(FlowLayout.LEFT));
        name_container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        Font font = new Font("Arial", Font.PLAIN, 20);
        user.setFont(font);
        user.setBorder(new EmptyBorder(10, 0, 0, 10));
        name_container.add(user);

        input_container.setBackground(Color.white);
        JTextField txtMessage = new JTextField();
        JButton btnSend = new JButton("Send");
        btnSend.setPreferredSize(new Dimension(80, 30));
        txtMessage.setPreferredSize(new Dimension(300, 30));
        input_container.add(txtMessage);
        input_container.add(btnSend);
        input_container.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));

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
        JScrollPane scrollPane = new JScrollPane(conversion);
        scrollPane.setBackground(Color.white);
        scrollPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane, gbc);

        gbc.gridy = 2;
        gbc.ipady = 1;
        panel.add(input_container, gbc);

        panel.setBackground(Color.white);

        btnSend.addActionListener(actionEvent -> {
            String content = txtMessage.getText().trim();
            if (!content.equals("")) {
                try {
                    dos.writeUTF(userName + "#" + content);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                messages.get(userName).addElement(new Message(content, true, LocalDate.now()));
                txtMessage.setText("");
            }
        });

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
        login_container.setBorder(new EmptyBorder(150, 20, 150, 20));
        JLabel welcome = new JLabel("P2P Chat Application");
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        Font font = new Font("SansSerif", Font.PLAIN, 30);
        welcome.setFont(font);
        login_container.add(welcome);
        txtLoginName = new JTextField();
        txtLoginName.setPreferredSize(new Dimension(400, 30));
        btnLogIn = new JButton("Let's go");
        btnLogIn.setPreferredSize(new Dimension(100, 30));
        JPanel container = new JPanel();
        container.add(txtLoginName);
        container.add(btnLogIn);
        login_container.add(container);

        // Navigation Container
        nav_container = new JTabbedPane();
        nav_container.setBackground(Color.white);
        nav_container.setBorder(new EmptyBorder(5, 5, 5, 5));
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
        active_container.setBackground(Color.white);
        JScrollPane conversion_container = new JScrollPane(conversionList);
        conversion_container.setBackground(Color.white);
        nav_container.add("Active", active_container);
        nav_container.add("Conversion", conversion_container);

        // Chat view
        chat_container = new JPanel();
        chat_container.setLayout(new GridLayout());
        chat_container.add(nav_container);

        chat_container.setBackground(Color.white);
        message_container = new JPanel();

        message_container.setBackground(Color.white);
        message_container.setLayout(cardLayout);
        chat_container.add(message_container);

        app_container.add("login", login_container);
        app_container.add("chat", chat_container);

        Container c = getContentPane();
        c.add(app_container);
    }
}
