package space.hvoal.ecologyassistant.data.category;

import java.util.Arrays;
import java.util.List;

public final class Categories {

    private Categories() {}

    public static final String CLEANUP = "CLEANUP";
    public static final String TREE_PLANTING = "TREE_PLANTING";
    public static final String RECYCLING = "RECYCLING";
    public static final String EDUCATION = "EDUCATION";
    public static final String ANIMALS = "ANIMALS";
    public static final String WATER_AIR = "WATER_AIR";
    public static final String OTHER = "OTHER";

    public static final class Item {
        public final String id;
        public final String title;

        public Item(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    public static List<Item> all() {
        return Arrays.asList(
                new Item(CLEANUP, "Уборка территории"),
                new Item(TREE_PLANTING, "Озеленение / посадка деревьев"),
                new Item(RECYCLING, "Раздельный сбор / переработка"),
                new Item(EDUCATION, "Просвещение / лекции"),
                new Item(ANIMALS, "Помощь животным"),
                new Item(WATER_AIR, "Вода / воздух"),
                new Item(OTHER, "Другое")
        );
    }
}
