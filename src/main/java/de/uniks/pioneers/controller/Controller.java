package de.uniks.pioneers.controller;

import javafx.scene.Parent;

public interface Controller {

    void init();

    void destroy();

    Parent render();
}
