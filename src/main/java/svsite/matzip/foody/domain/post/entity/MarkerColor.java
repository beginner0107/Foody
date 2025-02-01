package svsite.matzip.foody.domain.post.entity;

import lombok.Getter;

@Getter
public enum MarkerColor {
    RED("RED"),
    BLUE("BLUE"),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    PURPLE("PURPLE");

    private final String colorName;

    MarkerColor(String colorName) {
        this.colorName = colorName;
    }
}
