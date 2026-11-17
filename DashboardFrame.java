package elms.ui;

import elms.exceptions.LeaveException;
import elms.model.*;
import elms.service.ELMSService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DashboardFrame extends JFrame {
    private final ELMSService service;
    private final User user;
    private final JPanel content = new JPanel(new BorderLayout(14, 14));

    public DashboardFrame(ELMSService service, User user) {
        this.service = service;
        this.user = user;
        setTitle("ELMS | " + user.getDashboardTitle());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        buildLayout();
        showHome();
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG);
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.NAVY);
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(22, 15, 20, 15));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.add(UI.label("ELMS", 27, true, Color.WHITE));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(UI.label("LEAVE MANAGEMENT", 10, true, Theme.SIDEBAR_MUTED));
        sidebar.add(Box.createVerticalStrut(28));
        addNav(sidebar, "Overview", this::showHome);
        if (user instanceof Employee) {
            addNav(sidebar, "Apply for leave", this::showApply);
            addNav(sidebar, "My applications", this::showMyApplications);
            addNav(sidebar, "My leave balance", this::showBalances);
            addNav(sidebar, "Notifications", this::showNotifications);
        } else if (user instanceof Manager) {
            addNav(sidebar, "Pending requests", this::showPending);
            addNav(sidebar, "Team applications", this::showAllApplications);
            addNav(sidebar, "Notifications", this::showNotifications);
        } else if (user instanceof Admin) {
            addNav(sidebar, "Employees", this::showEmployees);
            addNav(sidebar, "Leave types", this::showLeaveTypes);
            addNav(sidebar, "All applications", this::showAllApplications);
            addNav(sidebar, "Attendance", this::showAttendance);
            addNav(sidebar, "Reports", this::showReports);
        }
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(UI.label(user.getName(), 13, true, Color.WHITE));
        sidebar.add(UI.label(user.getRole(), 10, false, Theme.SIDEBAR_MUTED));
        sidebar.add(Box.createVerticalStrut(14));
        addNav(sidebar, "Sign out", () -> {
            new LoginFrame(service).setVisible(true);
            dispose();
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        top.add(UI.label(user.getDashboardTitle(), 20, true, Theme.TEXT), BorderLayout.WEST);
        top.add(UI.label("Signed in as " + user.getName() + "   •   " + LocalDate.now(), 12, false, Theme.MUTED), BorderLayout.EAST);

        content.setBackground(Theme.BG);
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        root.add(sidebar, BorderLayout.WEST);
        root.add(top, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addNav(JPanel sidebar, String text, Runnable action) {
        JButton b = Theme.button(text, Theme.Kind.NAV);
        b.addActionListener(e -> action.run());
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void page(String title, String subtitle, JComponent body) {
        content.removeAll();
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(UI.label(title, 23, true, Theme.TEXT));
        heading.add(Box.createVerticalStrut(5));
        heading.add(UI.label(subtitle, 12, false, Theme.MUTED));
        content.add(heading, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.revalidate(); content.repaint();
    }

    private JPanel vertical() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JPanel stats(String[][] values) {
        JPanel p = new JPanel(new GridLayout(1, values.length, 12, 0));
        p.setOpaque(false);
        for (String[] v : values) {
            JPanel c = UI.card();
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.add(UI.label(v[0], 12, false, Theme.MUTED));
            c.add(Box.createVerticalStrut(10));
            c.add(UI.label(v[1], 27, true, Theme.BLUE));
            p.add(c);
        }
        return p;
    }

    private void showHome() {
        JPanel body = vertical();
        if (user instanceof Employee e) {
            ArrayList<LeaveBalance> balances = service.getBalances(e.getId());
            body.add(stats(new String[][]{
                    {"My applications", "" + service.applicationsFor(e).size()},
                    {"Pending", "" + service.applicationsFor(e).stream().filter(a -> a.getStatus()==LeaveApplication.Status.PENDING).count()},
                    {"Leave categories", "" + balances.size()}
            }));
            body.add(Box.createVerticalStrut(18));
            body.add(UI.label("Quick actions", 16, true, Theme.TEXT));
            body.add(Box.createVerticalStrut(10));
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); actions.setOpaque(false);
            JButton apply = Theme.button("＋  Apply for leave", true); apply.addActionListener(x -> showApply());
            JButton balance = Theme.button("View balances", false); balance.addActionListener(x -> showBalances());
            actions.add(apply); actions.add(balance); body.add(actions);
            body.add(Box.createVerticalStrut(20));
            body.add(UI.label("Recent applications", 16, true, Theme.TEXT));
            body.add(tablePanel(applicationRows(service.applicationsFor(e)), new String[]{"Reference","Leave type","From","To","Days","Status"}));
        } else if (user instanceof Manager m) {
            ArrayList<LeaveApplication> pending = service.pendingFor(m);
            body.add(stats(new String[][]{
                    {"Pending requests", "" + pending.size()},
                    {"Total employees", "" + service.getEmployees().stream().filter(e -> e.getManagerId().equals(m.getId())).count()},
                    {"Approved today / active", "" + service.employeesOnLeaveToday()}
            }));
            body.add(Box.createVerticalStrut(20));
            body.add(UI.label("Requests awaiting your decision", 16, true, Theme.TEXT));
            body.add(tablePanel(applicationRows(pending), new String[]{"Reference","Employee","Leave type","From","To","Days","Status"}));
        } else {
            body.add(stats(new String[][]{
                    {"Employees", "" + service.getEmployees().size()},
                    {"Pending requests", "" + service.countStatus(LeaveApplication.Status.PENDING)},
                    {"Approved", "" + service.countStatus(LeaveApplication.Status.APPROVED)},
                    {"On leave today", "" + service.employeesOnLeaveToday()}
            }));
            body.add(Box.createVerticalStrut(20));
            body.add(UI.label("System snapshot", 16, true, Theme.TEXT));
            JTextArea report = new JTextArea(service.reportSummary());
            report.setEditable(false); report.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(report); scroll.setBorder(new LineBorder(Theme.BORDER));
            body.add(scroll);
        }
        page("Good " + greeting() + ", " + user.getName().split(" ")[0], "Here is your Employee Leave Management System overview.", body);
    }

    private String greeting() {
        int h = java.time.LocalTime.now().getHour();
        return h < 12 ? "morning" : h < 17 ? "afternoon" : "evening";
    }

    private Object[][] applicationRows(ArrayList<LeaveApplication> apps) {
        Object[][] rows = new Object[apps.size()][7];
        for (int i=0;i<apps.size();i++) {
            LeaveApplication a=apps.get(i);
            rows[i]=new Object[]{a.getReference(), service.employeeName(a.getEmployeeId()), a.getLeaveType(),
                    a.getStartDate(), a.getEndDate(), a.getChargeableDays(), a.getStatus()};
        }
        return rows;
    }

    private JPanel tablePanel(Object[][] rows, String[] columns) {
        JPanel wrap = UI.card(); wrap.setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(30); table.setFont(Theme.font(12, false));
        table.getTableHeader().setFont(Theme.font(12, true));
        table.getTableHeader().setBackground(Theme.LIGHT_BLUE);
        table.setSelectionBackground(Theme.LIGHT_BLUE);
        wrap.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrap;
    }

    private void showApply() {
        Employee employee = (Employee) user;
        JPanel form = UI.card(); form.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        JComboBox<String> type = new JComboBox<>(service.getLeaveTypes().stream().map(LeaveType::getName).toArray(String[]::new));
        JTextField start = UI.field(18), end = UI.field(18), reason = UI.field(30), doc = UI.field(30);
        start.setText(LocalDate.now().plusDays(1).toString());
        end.setText(LocalDate.now().plusDays(1).toString());
        int r=0;
        addFormRow(form,g,r++,"Leave type",type);
        addFormRow(form,g,r++,"Start date (YYYY-MM-DD)",start);
        addFormRow(form,g,r++,"End date (YYYY-MM-DD)",end);
        addFormRow(form,g,r++,"Reason",reason);
        addFormRow(form,g,r++,"Supporting document path (optional)",doc);
        JButton submit=Theme.button("Submit application",true);
        g.gridx=1; g.gridy=r; form.add(submit,g);
        submit.addActionListener(e -> runAction(() -> {
            LeaveApplication a=service.applyForLeave(employee,(String)type.getSelectedItem(),
                    start.getText().trim(),end.getText().trim(),reason.getText(),doc.getText().trim());
            UI.message(this,"Application submitted successfully.\nReference: "+a.getReference()
                    +"\nChargeable working days: "+a.getChargeableDays(),JOptionPane.INFORMATION_MESSAGE);
            showMyApplications();
        }));
        page("Apply for leave","Complete the required fields. Weekends are excluded from chargeable days.",form);
    }

    private void addFormRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy=row; g.gridx=0; g.weightx=0;
        panel.add(UI.label(label,12,true,Theme.TEXT),g);
        g.gridx=1; g.weightx=1; panel.add(field,g);
    }

    private void showMyApplications() {
        Employee e=(Employee)user;
        JPanel body=vertical();
        body.add(tablePanel(applicationRows(service.applicationsFor(e)),
                new String[]{"Reference","Employee","Leave type","From","To","Days","Status"}));
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JTextField ref=UI.field(15); JButton cancel=Theme.button("Cancel pending application",Theme.Kind.DANGER);
        actions.add(UI.label("Application reference",12,true,Theme.TEXT)); actions.add(ref); actions.add(cancel);
        cancel.addActionListener(x -> runAction(() -> {
            service.cancelApplication(e,ref.getText().trim());
            UI.message(this,"Application cancelled.",JOptionPane.INFORMATION_MESSAGE); showMyApplications();
        }));
        body.add(Box.createVerticalStrut(10)); body.add(actions);
        page("My applications","View your leave history. Only pending requests can be cancelled.",body);
    }

    private void showBalances() {
        Employee e=(Employee)user;
        String[] cols={"Leave type","Year","Entitlement","Used","Remaining"};
        Object[][] rows=service.getBalances(e.getId()).stream().map(b ->
                new Object[]{b.getLeaveType(),b.getYear(),b.getEntitlement(),b.getUsed(),b.getRemaining()})
                .toArray(Object[][]::new);
        page("My leave balance","Your current-year entitlement and remaining leave.",tablePanel(rows,cols));
    }

    private void showPending() {
        Manager m=(Manager)user;
        ArrayList<LeaveApplication> apps=service.pendingFor(m);
        JPanel body=vertical();
        body.add(tablePanel(applicationRows(apps),new String[]{"Reference","Employee","Leave type","From","To","Days","Status"}));
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JTextField ref=UI.field(13), reason=UI.field(22);
        JButton approve=Theme.button("Approve",Theme.Kind.SUCCESS), reject=Theme.button("Reject",Theme.Kind.DANGER);
        actions.add(UI.label("Reference",12,true,Theme.TEXT)); actions.add(ref);
        actions.add(approve); actions.add(reject);
        body.add(Box.createVerticalStrut(10)); body.add(actions);
        body.add(UI.label("Rejection reason (required for rejection)",11,false,Theme.MUTED));
        body.add(reason);
        approve.addActionListener(e -> runAction(() -> {
            service.decide(m,ref.getText().trim(),true,"");
            UI.message(this,"Application approved and leave balance updated.",JOptionPane.INFORMATION_MESSAGE); showPending();
        }));
        reject.addActionListener(e -> runAction(() -> {
            service.decide(m,ref.getText().trim(),false,reason.getText());
            UI.message(this,"Application rejected.",JOptionPane.INFORMATION_MESSAGE); showPending();
        }));
        page("Pending requests","Review requests from employees assigned to you.",body);
    }

    private void showAllApplications() {
        ArrayList<LeaveApplication> apps=service.getApplications();
        if(user instanceof Manager m) apps.removeIf(a -> {
            User u=service.findUser(a.getEmployeeId());
            return !(u instanceof Employee) || !((Employee)u).getManagerId().equals(m.getId());
        });
        page(user instanceof Manager ? "Team applications":"All applications",
                "Application history and current decision status.",
                tablePanel(applicationRows(apps),new String[]{"Reference","Employee","Leave type","From","To","Days","Status"}));
    }

    private void showEmployees() {
        JPanel body=vertical();
        Object[][] rows=service.getEmployees().stream().map(e ->
                new Object[]{e.getId(),e.getName(),e.getUsername(),e.getEmail(),e.getDepartment(),e.getManagerId(),e.isActive()})
                .toArray(Object[][]::new);
        body.add(tablePanel(rows,new String[]{"ID","Name","Username","Email","Department","Manager ID","Active"}));
        JButton add=Theme.button("＋ Add employee",true);
        add.addActionListener(e -> addEmployeeDialog());
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT)); bar.setOpaque(false); bar.add(add);
        body.add(bar);
        page("Employee management","Register employees and assign them to a department and manager.",body);
    }

    private void addEmployeeDialog() {
        JTextField id=UI.field(18), name=UI.field(18), username=UI.field(18),
                password=UI.field(18), email=UI.field(18), dept=UI.field(18), manager=UI.field(18);
        JPanel p=new JPanel(new GridLayout(0,2,8,8));
        p.add(new JLabel("Employee ID"));p.add(id);p.add(new JLabel("Full name"));p.add(name);
        p.add(new JLabel("Username"));p.add(username);p.add(new JLabel("Temporary password"));p.add(password);
        p.add(new JLabel("Email"));p.add(email);p.add(new JLabel("Department"));p.add(dept);
        p.add(new JLabel("Manager ID"));p.add(manager);
        if(JOptionPane.showConfirmDialog(this,p,"Add employee",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)
            runAction(() -> { service.addEmployee(id.getText(),name.getText(),username.getText(),password.getText(),
                    email.getText(),dept.getText(),manager.getText());
                UI.message(this,"Employee created.",JOptionPane.INFORMATION_MESSAGE); showEmployees(); });
    }

    private void showLeaveTypes() {
        JPanel body=vertical();
        Object[][] rows=service.getLeaveTypes().stream().map(t ->
                new Object[]{t.getName(),t.getDefaultEntitlement(),t.isActive()}).toArray(Object[][]::new);
        body.add(tablePanel(rows,new String[]{"Leave type","Default entitlement (days)","Active"}));
        JButton add=Theme.button("＋ Add leave type",true);
        add.addActionListener(e -> {
            JTextField name=UI.field(15), days=UI.field(8);
            JPanel p=new JPanel(new GridLayout(2,2,8,8));
            p.add(new JLabel("Leave type"));p.add(name);p.add(new JLabel("Entitlement days"));p.add(days);
            if(JOptionPane.showConfirmDialog(this,p,"Add leave type",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)
                runAction(() -> { service.addLeaveType(name.getText(),Double.parseDouble(days.getText()));
                    UI.message(this,"Leave type added.",JOptionPane.INFORMATION_MESSAGE); showLeaveTypes(); });
        });
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));bar.setOpaque(false);bar.add(add);body.add(bar);
        page("Leave types and policies","Configure leave categories and default annual entitlements.",body);
    }

    private void showAttendance() {
        JPanel body=vertical();
        JTextField emp=UI.field(10), date=UI.field(12);
        date.setText(LocalDate.now().toString());
        JComboBox<String> status=new JComboBox<>(new String[]{"Present","Absent","On Leave"});
        JButton save=Theme.button("Save attendance",true);
        JPanel form=new JPanel(new FlowLayout(FlowLayout.LEFT));form.setOpaque(false);
        form.add(UI.label("Employee ID",12,true,Theme.TEXT));form.add(emp);
        form.add(UI.label("Date",12,true,Theme.TEXT));form.add(date);
        form.add(status);form.add(save);body.add(form);
        save.addActionListener(e -> runAction(() -> {
            service.markAttendance(emp.getText().trim(),date.getText().trim(),(String)status.getSelectedItem(),user.getId());
            UI.message(this,"Attendance saved.",JOptionPane.INFORMATION_MESSAGE);showAttendance();
        }));
        Object[][] rows=service.getAttendance().stream().map(a ->
                new Object[]{a.getEmployeeId(),service.employeeName(a.getEmployeeId()),a.getDate(),a.getStatus(),a.getRecordedBy()})
                .toArray(Object[][]::new);
        body.add(tablePanel(rows,new String[]{"Employee ID","Employee","Date","Status","Recorded by"}));
        page("Attendance management","Record daily attendance and review approved leave entries.",body);
    }

    private void showReports() {
        JTextArea area=new JTextArea(service.reportSummary());
        area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));area.setEditable(false);
        area.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        JButton refresh=Theme.button("Refresh report",false);
        refresh.addActionListener(e -> area.setText(service.reportSummary()));
        JButton export=Theme.button("Export report to TXT",true);
        export.addActionListener(e -> runAction(() -> {
            java.nio.file.Path path=java.nio.file.Path.of("data","ELMS-report-"+LocalDate.now()+".txt");
            try {
                java.nio.file.Files.createDirectories(path.getParent());
                java.nio.file.Files.writeString(path,service.reportSummary());
            } catch (java.io.IOException ex) {
                throw new IllegalStateException("Could not export report: " + ex.getMessage(), ex);
            }
            UI.message(this,"Report exported to:\n"+path.toAbsolutePath(),JOptionPane.INFORMATION_MESSAGE);
        }));
        JPanel body=new JPanel(new BorderLayout(8,8));body.setOpaque(false);
        body.add(new JScrollPane(area),BorderLayout.CENTER);
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));bar.setOpaque(false);bar.add(refresh);bar.add(export);
        body.add(bar,BorderLayout.SOUTH);
        page("Reports and analytics","Dashboard totals and department-wise employee summary.",body);
    }

    private void showNotifications() {
        ArrayList<Notification> notes=service.getNotifications(user.getId());
        Object[][] rows=notes.stream().map(n ->
                new Object[]{n.getCreatedAt(),n.getMessage(),n.isRead()?"Read":"Unread"})
                .toArray(Object[][]::new);
        JPanel body=vertical();
        body.add(tablePanel(rows,new String[]{"Date and time","Message","Status"}));
        JButton read=Theme.button("Mark all as read",true);
        read.addActionListener(e -> {service.markNotificationsRead(user.getId());showNotifications();});
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));bar.setOpaque(false);bar.add(read);body.add(bar);
        page("Notifications","Updates about submitted leave applications and decisions.",body);
    }

    private void runAction(Runnable action) {
        try { action.run(); }
        catch (LeaveException | IllegalArgumentException ex) {
            UI.message(this,ex.getMessage(),JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            UI.message(this,"Operation failed: "+ex.getMessage(),JOptionPane.ERROR_MESSAGE);
        }
    }
}
