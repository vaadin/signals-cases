package com.example.views;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/**
 * A profile picture avatar wrapped in a colored ring border. Used to visually
 * differentiate user sessions by their assigned color.
 */
public class ColoredAvatar extends Div {

    public ColoredAvatar(@Nullable String username, String color, int sizePx) {
        Image img = new Image(MainLayout.getProfilePicturePath(username), "");
        int imgSize = sizePx - 6; // account for 3px border on each side
        img.setWidth(imgSize + "px");
        img.setHeight(imgSize + "px");
        img.getStyle().set("border-radius", "50%").set("object-fit", "cover")
                .set("display", "block");

        add(img);
        getStyle().set("width", sizePx + "px").set("height", sizePx + "px")
                .set("border-radius", "50%")
                .set("border", "3px solid " + color).set("flex-shrink", "0")
                .set("box-sizing", "border-box");
    }
}
