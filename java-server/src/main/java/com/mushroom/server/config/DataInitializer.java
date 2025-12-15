package com.mushroom.server.config;

import com.mushroom.server.model.Category;
import com.mushroom.server.model.MushroomReference;
import com.mushroom.server.repository.CategoryRepository;
import com.mushroom.server.repository.MushroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MushroomRepository mushroomRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Инициализация Категорий
        if (categoryRepository.count() == 0) {
            List<Category> cats = Arrays.asList(
                    createCat("Свежие грибы"),
                    createCat("Сушеные грибы"),
                    createCat("Маринованные грибы"),
                    createCat("Лекарственные грибы"),
                    createCat("Грибница (Мицелий)"),
                    createCat("Инвентарь")
            );
            categoryRepository.saveAll(cats);
            System.out.println(">>> Категории успешно загружены в БД");
        }

        // 2. Инициализация Справочника грибов
        if (mushroomRepository.count() == 0) {
            List<MushroomReference> mushrooms = Arrays.asList(
                    createMush("penny_bun", "Белый гриб", true,
                            "Царь грибов. Толстая ножка, коричневая шляпка. Мякоть белая.",
                            "Универсален. Суп, жарка, маринад."),

                    createMush("chanterelle", "Лисичка", true,
                            "Ярко-желтый гриб с волнистой шляпкой. Никогда не бывает червивым.",
                            "Вкусна жареной со сметаной."),

                    createMush("fly_agaric", "Мухомор красный", false,
                            "Красная шляпка с белыми точками. Ядовит.",
                            "ОПАСЕН! Содержит токсины."),

                    createMush("brown_birch_bolete", "Подберезовик", true,
                            "Растет в березовых рощах. Шляпка серо-коричневая.",
                            "Подходит для сушки (чернеет) и жарки."),

                    createMush("slippery_jack", "Маслёнок", true,
                            "Гриб с маслянистой клейкой шляпкой.",
                            "Обязательно снять кожицу со шляпки перед варкой."),

                    createMush("agaricus", "Шампиньон", true,
                            "Популярный магазинный гриб.",
                            "Можно жарить, варить и есть сырым.")
            );
            mushroomRepository.saveAll(mushrooms);
            System.out.println(">>> Справочник грибов загружен в БД");
        }
    }

    private Category createCat(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }

    private MushroomReference createMush(String name, String disp, boolean edible, String desc, String tips) {
        MushroomReference m = new MushroomReference();
        m.setName(name);
        m.setDisplayName(disp);
        m.setIsEdible(edible);
        m.setDescription(desc);
        m.setCookingTips(tips);
        return m;
    }
}
