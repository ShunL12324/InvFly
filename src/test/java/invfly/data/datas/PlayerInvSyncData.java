package invfly.data.datas;

import com.google.gson.Gson;
import invfly.data.GsonTypes;
import invfly.data.SyncData;
import javafx.util.Pair;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerInvSyncData implements SyncData {

    private final Gson gson = new Gson();

    @Override
    public String getSerializedData(User user) {
        List<Pair<Integer, String>> list = new ArrayList<>();
        user.getInventory().slots().forEach(inventory -> {
            inventory.peek().ifPresent(itemStack -> {
                inventory.getInventoryProperty(SlotIndex.class).ifPresent(slotIndex -> {
                    if (slotIndex.getValue() != null){
                        int index = slotIndex.getValue();
                        try {
                            String data = DataFormats.JSON.write(itemStack.toContainer());
                            list.add(new Pair<>(index, data));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                });
            });
        });
        return gson.toJson(list, GsonTypes.INVTYPE);
    }

    @Override
    public void deserialize(User user, String data) {

        List<Pair<Integer, String>> pairs = gson.fromJson(data, GsonTypes.INVTYPE);
        Inventory inventory = user.getInventory();
        this.deserialize(inventory, pairs);

    }

    protected void deserialize(Inventory inventory, List<Pair<Integer, String>> pairs){
        inventory.clear();
        pairs.forEach(integerStringPair -> {
            try {
                DataContainer container = DataFormats.JSON.read(integerStringPair.getValue());
                ItemStack itemStack = ItemStack.builder().fromContainer(container).build();
                inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(integerStringPair.getKey()))).set(itemStack);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getID() {
        return "playerinventory";
    }
}
