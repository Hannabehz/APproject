package dto;

import java.util.List;
import java.util.UUID;

public class OrderDTO {

    private UUID id;
    private String deliveryAddress;
    private UUID vendorId;
    private List<OrderItemDTO> items;

    // Constructors
    public OrderDTO() {}

    public OrderDTO(UUID id, String deliveryAddress, UUID vendorId, List<OrderItemDTO> items) {
        this.id = id;
        this.deliveryAddress = deliveryAddress;
        this.vendorId = vendorId;
        this.items = items;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }

    public List<OrderItemDTO> getItems() { return items; }
public void setItems(List<OrderItemDTO> items) { this.items = items; }

}

