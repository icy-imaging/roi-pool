package plugins.fab.ROIPool;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class TextDialog extends JDialog  implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 851158327366248302L;

	private JTextField textField;

	private JOptionPane optionPane;
	private String btnString1 = "Rename";
	private String btnString2 = "Cancel";
	
	public TextDialog( String title , String message , String defaultText ) {
        
		setTitle( title );

        textField = new JTextField(10);
        textField.setText( defaultText );

        Object[] array = { message , textField };

        Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[1]);

        setContentPane(optionPane);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    optionPane.setValue(new Integer( JOptionPane.CLOSED_OPTION));
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener( this );
        
        optionPane.addPropertyChangeListener(this);
        setModal( true );
        
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getSource() == textField )
		{
			isOk=true; // FIXME : rush quickfix. Should not be here but in propertychange.
			optionPane.setValue(new Integer( JOptionPane.YES_OPTION)); // FIXME : the property does not get this
		}
	}

	public boolean isOk = false;
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (btnString1.equals( optionPane.getValue() ) ) 
		{
			isOk = true;
		}
		
		setVisible(false);

	}

	public String getText() {
		
		return textField.getText();
		
	}

	
}
