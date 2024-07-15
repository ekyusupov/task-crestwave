package ru.iusupov.crestwave.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import ru.iusupov.crestwave.models.User;
import ru.iusupov.crestwave.services.UserService;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Ernest Iusupov
 */
@Route("")
public class UsersView extends VerticalLayout {
    private final UserService userService;
    private final Grid<User> userGrid;
    private final TextField nameFilter = new TextField("Поиск по имени");;
    private final TextField descriptionFilter = new TextField("Поиск по описанию");
    private final Map<String, TextField> createUserFields;

    public UsersView(UserService userService) {
        this.userService = userService;
        userGrid = createUserGrid();
        createFilters();
        createUserFields = createFields();
        Dialog dialog = createDialog();
        updateUserGrid();
        add(createForm(dialog), userGrid);
    }

    private Component createForm(Dialog dialog) {
        Button createButton = new Button("Создать", event -> dialog.open());
        Button saveButton = new Button("Сохранить", event -> {
            userService.saveAll();
            updateUserGrid();
        } );
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        var form = new HorizontalLayout(
                nameFilter,
                descriptionFilter,
                createButton,
                saveButton
        );
        form.setAlignItems(Alignment.BASELINE);
        return form;
    }

    private Dialog createDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Создать пользователя");
        Binder<User> binder = new Binder<>(User.class);

        VerticalLayout dialogLayout = new VerticalLayout(createUserFields.values().toArray(new TextField[0]));
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.getStyle().set("width", "18rem").set("max-width", "100%");

        int minLength = 1;
        binder.forField(createUserFields.get("id"))
                .withValidator( value -> value.length() >= 1,
                        "Минимум %s символ".formatted(1))
                .withConverter(new StringToLongConverter("Введите корректный ID"))
                .bind("id");
        bind(binder, createUserFields.get("name"), "name", minLength);
        bind(binder, createUserFields.get("email"), "email", minLength);
        bind(binder, createUserFields.get("phone"), "phone", minLength);
        bind(binder, createUserFields.get("description"), "description", 0);

        dialog.add(dialogLayout);

        Button addButton = new Button("Сохранить", event -> {
            User user = new User();
            try {
                binder.writeBean(user);
                userService.save(user);
                updateUserGrid();
                dialog.close();
                cleanFields();
            } catch (ValidationException e) {
                System.err.println("Validation error: " + e.getMessage());
            }
        });
        addButton.addClickShortcut(Key.ENTER);
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отменить", event -> {
            dialog.close();
            cleanFields();
        });
        dialog.getFooter().add(cancelButton);
        dialog.getFooter().add(addButton);
        return dialog;
    }

    private Grid<User> createUserGrid() {
        Grid<User> grid = new Grid<>(User.class);
        grid.removeAllColumns();
        grid.addColumn("id").setHeader("Id");
        grid.addColumn("name").setHeader("Имя");
        grid.addColumn("email").setHeader("Email");
        grid.addColumn("phone").setHeader("Телефон");
        grid.addColumn("description").setHeader("Описание");
        grid.addItemDoubleClickListener(event -> {
           showDetails(event.getItem());
        });
        return grid;
    }

    private void createFilters() {
        nameFilter.setPlaceholder("Имя...");
        nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameFilter.addInputListener(event -> {
            updateUserGrid();
        });
        descriptionFilter.setPlaceholder("Описание...");
        descriptionFilter.setValueChangeMode(ValueChangeMode.EAGER);
        descriptionFilter.addInputListener(event -> {
            updateUserGrid();
        });
    }

    private void showDetails(User user) {
        Dialog dialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();

        TextField id = new TextField("ID");
        id.setValue(String.valueOf(user.getId()));
        id.setReadOnly(true);

        TextField name = new TextField("Name");
        name.setValue(user.getName());
        name.setReadOnly(true);

        TextField email = new TextField("Email");
        email.setValue(user.getEmail());
        email.setReadOnly(true);

        TextField phone = new TextField("Phone");
        phone.setValue(user.getPhone());
        phone.setReadOnly(true);

        TextField description = new TextField("Description");
        description.setValue(user.getDescription());
        description.setReadOnly(true);

        layout.add(id, name, email, phone, description);

        Button closeButton = new Button("Закрыть", e -> dialog.close());
        dialog.add(layout, closeButton);
        dialog.open();
    }

    private void updateUserGrid() {
        List<User> users = userService.getUsers().stream()
                .filter(u -> u.getName().contains(nameFilter.getValue()))
                .filter(u -> u.getDescription().contains(descriptionFilter.getValue()))
                .collect(Collectors.toList());
        userGrid.setItems(users);
    }

    private static void bind(Binder<User> binder, TextFieldBase<?, String> field, String property, int minLength) {
        binder.forField(field).withValidator(
                value -> value.length() >= minLength,
                "Минимум %s символ".formatted(minLength)
        ).bind(property);
    }

    private Map<String, TextField> createFields() {
        Map<String, TextField> fieldMap = new LinkedHashMap<>();
        fieldMap.put("id", new TextField("ID"));
        fieldMap.put("name", new TextField("Имя"));
        fieldMap.put("email", new TextField("Email"));
        fieldMap.put("phone", new TextField("Телефон"));
        fieldMap.put("description", new TextField("Описание"));
        return fieldMap;
    }

    private void cleanFields() {
        createUserFields.values().forEach(f -> {
            f.clear();
            f.setInvalid(false);
            f.setErrorMessage(null);
        });
    }
}
