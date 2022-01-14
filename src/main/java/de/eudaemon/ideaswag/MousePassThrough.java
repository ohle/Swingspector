package de.eudaemon.ideaswag;

import java.awt.Component;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

class MousePassThrough implements MouseListener, MouseMotionListener, MouseWheelListener {
    private final Component target;

    public MousePassThrough(Component target_) {
        target = target_;
    }

    public void register(Component source) {
        source.addMouseListener(this);
        source.addMouseMotionListener(this);
        source.addMouseWheelListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        target.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, target));
    }
}
