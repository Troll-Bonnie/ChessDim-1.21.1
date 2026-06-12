package net.baphy.chessdim.world;

public enum CellType {
    DEFAULT,
    WATER,
    VOID,
    STONE,
    NETHER;

    public int getParticleColor(){
        return switch (this){
            case DEFAULT -> 0xFFFFFF;
            case WATER   -> 0x3399FF;
            case VOID    -> 0x333333;
            case STONE   -> 0x888888;
            case NETHER  -> 0xFF3300;
        };
    }

    public int getTextColor(){
        return switch (this){
            case DEFAULT -> 0xFFFFFF;
            case WATER   -> 0x3399FF;
            case VOID    -> 0x555555;
            case STONE   -> 0xAAAAAA;
            case NETHER  -> 0xFF3300;
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
