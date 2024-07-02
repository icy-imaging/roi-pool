/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.fab.ROIPool;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TextDialog extends JDialog implements ActionListener, PropertyChangeListener {
    private final JTextField textField;

    private final JOptionPane optionPane;
    private final String btnString1 = "Rename";
    private final String btnString2 = "Cancel";

    public TextDialog(final String title, final String message, final String defaultText) {
        setTitle(title);

        textField = new JTextField(10);
        textField.setText(defaultText);

        final Object[] array = {message, textField};

        final Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[1]);

        setContentPane(optionPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);

        optionPane.addPropertyChangeListener(this);
        setModal(true);
    }

    @Override
    public void actionPerformed(final @NotNull ActionEvent e) {
        if (e.getSource() == textField) {
            isOk = true; // FIXME : rush quickfix. Should not be here but in propertychange.
            optionPane.setValue(JOptionPane.YES_OPTION); // FIXME : the property does not get this
        }
    }

    public boolean isOk = false;

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {

        if (btnString1.equals(optionPane.getValue())) {
            isOk = true;
        }

        setVisible(false);
    }

    public String getText() {
        return textField.getText();
    }
}
