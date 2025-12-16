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
                    // 1. Польский гриб (Bay Bolete)
                    createMush("bay_bolete", "Польский гриб", true,
                            "Похож на белый, но при надавливании мякоть синеет. Коричневая шляпка.",
                            "Отличный вкус. Подходит для сушки, жарки и супов."),

                    // 2. Подберезовик (Brown Birch Bolete)
                    createMush("brown_birch_bolete", "Подберезовик", true,
                            "Растет в березовых рощах. Шляпка серо-коричневая, ножка в чешуйках.",
                            "Подходит для сушки (чернеет) и жарки."),

                    // 3. Лисичка (Chanterelle)
                    createMush("chanterelle", "Лисичка", true,
                            "Ярко-желтый гриб с волнистой шляпкой. Никогда не бывает червивым.",
                            "Вкусна жареной со сметаной. Не требует долгой варки."),

                    // 4. Сыроежка сине-зеленая (Charcoal Burner)
                    createMush("charcoal_burner", "Сыроежка", true,
                            "Шляпка с переливами фиолетового и зеленого. Пластинки гибкие, не ломкие.",
                            "Универсальна. Можно варить, жарить и солить."),

                    // 5. Сморчок (Common Morel)
                    createMush("common_morel", "Сморчок", true,
                            "Весенний гриб со сморщенной ячеистой шляпкой.",
                            "Условно съедобен! Требует предварительного отваривания (отвар слить)."),

                    // 6. Бледная поганка (Death Cap)
                    createMush("deathcap", "Бледная поганка", false,
                            "Бледно-зеленая шляпка, юбочка на ножке и мешочек (вольва) у основания.",
                            "СМЕРТЕЛЬНО ЯДОВИТ! Противоядия нет. Даже кусочек убивает."),

                    // 7. Ложная лисичка (False Chanterelle)
                    createMush("false_chanterelle", "Ложная лисичка", false,
                            "Ярко-оранжевый цвет, тонкие частые пластинки. Ножка тоньше, чем у настоящей.",
                            "Несъедобен. Невкусный, может вызвать расстройство желудка."),

                    // 8. Шампиньон полевой (Field Mushroom)
                    createMush("field_mushroom", "Шампиньон полевой", true,
                            "Белая шляпка, розовые (у молодых) или коричневые пластинки.",
                            "Можно жарить, варить, мариновать. Вкуснее магазинных."),

                    // 9. Мухомор красный (Fly Agaric)
                    createMush("fly_agaric", "Мухомор красный", false,
                            "Красная шляпка с белыми точками. Классический ядовитый гриб.",
                            "ОПАСЕН! Содержит токсины и галлюциногены."),

                    // 10. Дождевик гигантский (Giant Puffball)
                    createMush("giant_puffball", "Дождевик гигантский", true,
                            "Огромный белый шар. Съедобен, пока мякоть внутри идеально белая.",
                            "Жарят ломтиками в кляре, вкус напоминает нежное мясо."),

                    // 11. Опёнок осенний (Honey Fungus)
                    createMush("honey_fungus", "Опёнок", true,
                            "Растут «семьями» на пнях и деревьях. На ножке есть кольцо.",
                            "Идеальны для маринования и жарки с картошкой."),

                    // 12. Подосиновик (Orange Birch Bolete)
                    createMush("orange_birch_bolete", "Подосиновик", true,
                            "Яркая оранжево-красная шляпка. На срезе ножка быстро синеет.",
                            "Плотный, сытный гриб. Вкусен в любом виде."),

                    // 13. Вешенка (Oyster Mushroom)
                    createMush("oyster_mushroom", "Вешенка", true,
                            "Растет на деревьях группами. Уховидная форма, сероватый цвет.",
                            "Часто продается в магазинах. Хороша для жарки и пирогов."),

                    // 14. Гриб-зонтик (Parasol)
                    createMush("parasol", "Гриб-зонтик", true,
                            "Высокий гриб с большой чешуйчатой шляпкой и подвижным кольцом.",
                            "Шляпку жарят целиком в кляре, как шницель. Деликатес."),

                    // 15. Белый гриб (Penny Bun)
                    createMush("penny_bun", "Белый гриб", true,
                            "Царь грибов. Толстая ножка, коричневая шляпка. Мякоть белая.",
                            "Универсален. Суп, жарка, маринад, сушка."),

                    // 16. Рыжик (Saffron Milkcap)
                    createMush("saffron_milkcap", "Рыжик", true,
                            "Оранжевый гриб с концентрическими кругами. Выделяет оранжевый сок.",
                            "Лучший гриб для засолки. Можно есть соленым без варки."),

                    // 17. Маслёнок (Slippery Jack)
                    createMush("slippery_jack", "Маслёнок", true,
                            "Гриб с маслянистой клейкой шляпкой. Трубчатый слой желтый.",
                            "Обязательно снять кожицу со шляпки перед готовкой."),

                    // 18. Сыроежка жгучеедкая (The Sickener)
                    createMush("the_sickener", "Сыроежка жгучая", false,
                            "Ярко-красная шляпка. Мякоть очень ломкая.",
                            "Ядовита. Имеет очень острый, жгучий вкус, вызывает рвоту."),

                    // 19. Волнушка (Woolly Milkcap)
                    createMush("woolly_milkcap", "Волнушка", true,
                            "Розовая шляпка с мохнатыми краями. Выделяет белый горький сок.",
                            "Только для засолки! Требует вымачивания и отваривания."),

                    // 20. Шампиньон желтокожий (Yellow Stainer)
                    createMush("yellow_stainer", "Шампиньон желтокожий", false,
                            "Похож на обычный, но желтеет при надавливании (особенно у основания).",
                            "Ядовит! При варке пахнет карболкой (чернилами/аптекой).")
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
