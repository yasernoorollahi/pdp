package com.datarain.pdp.item.specification;

import com.datarain.pdp.item.entity.Item;
import com.datarain.pdp.item.entity.ItemStatus;
import com.datarain.pdp.item.entity.ItemType;
import org.springframework.data.jpa.domain.Specification;

/**
 * اضافه شد: Specification Pattern برای dynamic query روی Item
 * به جای اینکه برای هر ترکیب filter یه متد جداگانه بنویسیم، از Specification استفاده میکنیم
 */
public class ItemSpecification {

    private ItemSpecification() {}

    public static Specification<Item> hasStatus(ItemStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Item> hasType(ItemType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Item> titleContains(String keyword) {
        return (root, query, cb) ->
                (keyword == null || keyword.isBlank()) ? null :
                        cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Item> isEnabled() {
        return (root, query, cb) -> cb.isTrue(root.get("enabled"));
    }

    // اضافه شد: فیلتر soft-delete - فقط آیتم‌های active و enabled
    public static Specification<Item> activeOnly() {
        return hasStatus(ItemStatus.ACTIVE).and(isEnabled());
    }
}
