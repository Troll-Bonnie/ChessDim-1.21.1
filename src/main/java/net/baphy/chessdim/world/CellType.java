package net.baphy.chessdim.world;

import java.awt.*;

public enum CellType {
    DEFAULT,
    WATER,
    VOID,
    STONE,
    NETHER,
    GRASS;

    public int getParticleColor(){
        return switch (this){
            case DEFAULT -> new Color(255, 255, 255).getRGB();
            case WATER   -> new Color(51, 153, 255).getRGB();
            case VOID    -> new Color(51, 51, 51).getRGB();
            case STONE   -> new Color(136, 136, 136).getRGB();
            case NETHER  -> new Color(255, 51, 0).getRGB();
            case GRASS   -> new Color(85, 170, 0).getRGB();
        };
    }

    public int getTextColor(){
        return switch (this){
            case DEFAULT -> new Color(255, 255, 255).getRGB();
            case WATER   -> new Color(51, 153, 255).getRGB();
            case VOID    -> new Color(85, 85, 85).getRGB();
            case STONE   -> new Color(170, 170, 170).getRGB();
            case NETHER  -> new Color(255, 51, 0).getRGB();
            case GRASS   -> new Color(85, 170, 0).getRGB();
        };
    }

    public String getTranslationKey(){
        return "chessdim.cell_type."+name().toLowerCase();
    }

    public CellType next() {
        CellType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public CellType prev() {
        CellType[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

}
