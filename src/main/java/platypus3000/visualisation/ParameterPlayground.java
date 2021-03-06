package platypus3000.visualisation;

import processing.core.PApplet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * For tuning parameters on the fly
 */
public class ParameterPlayground extends JPanel implements Scrollable
{
    private HashMap<String, JPanel> groups = new HashMap<String, JPanel>();
    private JPanel defaultGroup;
    private JFrame rootWindow = null;

    private static ParameterPlayground instance = null;
    public static boolean showParameterPlayground = true;
    public static ParameterPlayground getInstance()
    {
        if(instance == null && showParameterPlayground)
        {
            instance = new ParameterPlayground();
        }
        return instance;
    }

    private ParameterPlayground()
    {
//        setSize(200, 1000);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        defaultGroup = new JPanel();
        defaultGroup.setLayout(new BoxLayout(defaultGroup, BoxLayout.PAGE_AXIS));
        defaultGroup.setBorder(BorderFactory.createTitledBorder(""));

        add(defaultGroup);



//        final JComboBox fieldSelector = new JComboBox(Debugger.getFields().toArray());
//        fieldSelector.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent actionEvent) {
//                Debugger.setDisplayedField((String)fieldSelector.getSelectedItem());
//            }
//        });
//        Debugger.setFieldsChangeListener(new FieldsChangeListener() {
//            @Override
//            public void onNewField(String field) {
//                fieldSelector.addItem(field);
//            }
//        });

//        defaultGroup.add(fieldSelector);

    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(350,400);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle rectangle, int i, int i2) {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle rectangle, int i, int i2) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private class Option
    {
        final String name, group;
        final JCheckBox checkBox;

        private Option(final OptionListener listener, final String name, final String group, boolean initialValue) {
            //Init the values
            this.name = name;
            this.group = group;

            //Create a checkbox
            this.checkBox = new JCheckBox(name);
            this.checkBox.setSelected(initialValue);
            this.checkBox.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    listener.optionChanged(checkBox.isSelected(), name, group);
                }
            });

            //Create a JPanel to hold the checkbox and some extra space
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
            p.add(checkBox);
            p.add(Box.createVerticalStrut(10));


            ParameterPlayground.this.getGroupPanel(group).add(p);
            if(rootWindow != null) rootWindow.pack();
        }

        @Override
        public int hashCode() {
            return (name + group).hashCode();
        }
    }

    public static void addOption(OptionListener listener, String name, boolean initialValue)
    {
        addOption(listener, name, null, initialValue);
    }

    public static void addOption(OptionListener listener, String name, String group, boolean initialValue)
    {
        if(getInstance() != null)
            getInstance().addOpt(listener, name, group, initialValue);
    }

    public static void addOption(final Object obj, String name)
    {
        addOption(obj, name, null);
    }

    public static void addOption(final Object obj, final String name, String group, String prettyName)
    {
        addOption(new OptionListener() {
            @Override
            public void optionChanged(boolean newValue, String n, String g) {
                setBoolMember(obj, name, newValue);
            }
        }, prettyName, group, getBoolMember(obj, name));
    }

    public static void addOption(final Object obj, String name, String group)
    {
        addOption(obj, name, group, name);
    }

    public void addOpt(OptionListener listener, String name, String group, boolean initialValue)
    {
        new Option(listener, name, group, initialValue);
    }

    public static void addParameter(ParameterListener listener, float min, float max, int steps, String name, float initialValue)
    {
        if(getInstance() != null)
            getInstance().addParam(listener, min, max, steps, name, null, initialValue);
    }

    public static void addParameter(ParameterListener listener, float min, float max, String name, float initialValue)
    {
        addParameter(listener, min, max, name, null, initialValue);
    }

    public static void addParameter(ParameterListener listener, float min, float max, String name, String group, float initialValue)
    {
        if(getInstance() != null)
            getInstance().addParam(listener, min, max, 100, name, group, initialValue);
    }

    public static void addParameter(ParameterListener listener, float min, float max, int steps, String name, String group, float initialValue)
    {
        if(getInstance() != null)
            if(getInstance() != null)getInstance().addParam(listener, min, max, steps, name, group, initialValue);
    }

    public static void addParameter(final Object obj, float min, float max, String name, String prettyName, String group) {
        addParameter(obj, min, max, 100, name, prettyName, group);
    }

    public static void addParameter(final Object obj, float min, float max, String name, String group)
    {
        addParameter(obj, min, max, 100, name, name, group);
    }

    public static void addParameter(final Object obj, float min, float max, String name)
    {
        addParameter(obj, min, max, 100, name, name, null);
    }

    public static void addParameter(final Object obj, float min, float max, int steps, final String name, String prettyName, String group)
    {
        addParameter(new ParameterListener() {
            @Override
            public void parameterChanged(float newValue, String n, String group) {
                setFloatMember(obj, name, newValue);
            }
        }, min, max, steps, prettyName, group, getFloatMember(obj, name));
    }

    private static float getFloatMember(Object obj, String member)
    {
        Class theClass;
        if(obj instanceof Class)
            theClass = (Class) obj;
        else
            theClass = obj.getClass();

        try {
            return theClass.getField(member).getFloat(obj);
        } catch (IllegalAccessException e) {
            System.err.printf("%s in %s must be public!\n", member, obj.getClass().getName());
        } catch (NoSuchFieldException e) {
            System.err.printf("%s is final or not a member variable of %s!\n", member, obj.getClass().getName());
        }
        return 0;
    }

    private static boolean getBoolMember(Object obj, String member)
    {
        Class theClass;
        if(obj instanceof Class)
            theClass = (Class) obj;
        else
            theClass = obj.getClass();

        try {
            return theClass.getField(member).getBoolean(obj);
        } catch (IllegalAccessException e) {
            System.err.printf("%s in %s must be public!\n", member, obj.getClass().getName());
        } catch (NoSuchFieldException e) {
            System.err.printf("%s is not a member variable of %s!\n", member, obj.getClass().getName());
        }
        return false;
    }

    private static void setFloatMember(Object obj, String member, float newVal)
    {
        Class theClass;
        if(obj instanceof Class)
            theClass = (Class) obj;
        else
            theClass = obj.getClass();

        try {
            theClass.getField(member).setFloat(obj, newVal);
        } catch (IllegalAccessException e) {
            System.err.printf("%s in %s must be public!\n", member, obj.getClass().getName());
        } catch (NoSuchFieldException e) {
            System.err.printf("%s is not a member variable of %s!\n", member, obj.getClass().getName());
        }
    }

    private static void setBoolMember(Object obj, String member, boolean newVal)
    {
        Class theClass;
        if(obj instanceof Class)
            theClass = (Class) obj;
        else
            theClass = obj.getClass();

        try {
            theClass.getField(member).setBoolean(obj, newVal);
        } catch (IllegalAccessException e) {
            System.err.printf("%s in %s must be public!\n", member, obj.getClass().getName());
        } catch (NoSuchFieldException e) {
            System.err.printf("%s is not a member variable of %s!\n", member, obj.getClass().getName());
        }
    }

    public void addParam(ParameterListener listener, float min, float max, int steps, String name, String group, float initialValue)
    {
        new Parameter(listener, min, max, steps, name, group, initialValue);
    }

    public JPanel getGroupPanel(String groupName)
    {
        if(groupName == null)
            return defaultGroup;

        if(groups.get(groupName) == null)
        {
            JPanel newGroup = new JPanel();
            newGroup.setLayout(new BoxLayout(newGroup, BoxLayout.PAGE_AXIS));
            newGroup.setBorder(BorderFactory.createTitledBorder(groupName));
            add(newGroup);
            groups.put(groupName, newGroup);
        }

        return groups.get(groupName);
    }

    private class Parameter {
        private final int darkness = 210;
        private final Color color = new Color(darkness, darkness, darkness);
        final ParameterListener listener;
        final float min, max;
        final int steps;
        final String name, group;
        final JSlider slider;

        public Parameter(ParameterListener l, final float min, final float max, final int steps, final String name, final String group, float initialValue)
        {
            //Init the values
            this.listener = l;
            this.min = min;
            this.max = max;
            this.steps = steps;
            this.name = name;
            this.group = group;

            //Create a slider
            final int numMajorTics = 5;
            slider = new JSlider(0, steps);
            slider.setValue((int)PApplet.map(initialValue, min, max, 0, steps));
            slider.setBackground(color);
            slider.setOpaque(true);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setPaintTrack(false);
            slider.setMinorTickSpacing(steps / (numMajorTics * 5));
            slider.setMajorTickSpacing(steps / numMajorTics);
            //Create some custom tics
            Dictionary<Integer, JLabel> labels = new Hashtable<Integer, JLabel>(numMajorTics + 1);
            for(int i = 0; i <= numMajorTics; i++)
            {
                String labelString = Float.toString(PApplet.map(i, 0, numMajorTics, min, max));
                labelString = new DecimalFormat("#.##").format(PApplet.map(i, 0, numMajorTics, min, max));
                labels.put(steps / numMajorTics * i, new JLabel(labelString));
            }
            slider.setLabelTable(labels);
            //Attach a glue listener to the slider that does the value conversion (because JSlider only does ints!!!
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    float val = PApplet.map(slider.getValue(), 0, steps, min, max);
                    listener.parameterChanged(val, name, group);
                }
            });
            slider.setPreferredSize(new Dimension(300, (int) slider.getPreferredSize().getHeight()));

            //Create a Panel with the name, the slider and some space at the bottom
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);

            //Label
            JLabel nameLabel = new JLabel(name);
            nameLabel.setLabelFor(slider);
            nameLabel.setBackground(color);
            nameLabel.setOpaque(true);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(nameLabel, BorderLayout.PAGE_START);

            //Slider
            slider.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(slider, BorderLayout.PAGE_END);

            //Some space
            p.add(Box.createVerticalStrut(10));

            //Add the Panel to the correct group panel
            ParameterPlayground.this.getGroupPanel(group).add(p);
            if(rootWindow != null) rootWindow.pack();
        }

        @Override
        public int hashCode() {
            return (name + group).hashCode();
        }
    }

    public void setRootWindow(SettingsWindow mw)
    {
        this.rootWindow = mw;
    }


    public interface OptionListener
    {
        void optionChanged(boolean newValue, String name, String group);
    }

    public interface ParameterListener
    {
        void parameterChanged(float newValue, String name, String group);
    }


}
