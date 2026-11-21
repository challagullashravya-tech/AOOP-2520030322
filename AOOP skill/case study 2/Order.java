
import java.time.Instant;
import java.util.*;

final class TraditionalOrder {
    private final OrderId id;
    private Status status = Status.DRAFT;
    private final Map<ProductId, OrderLine> lines = new LinkedHashMap<>();
    private Optional<Shipment> shipment = Optional.empty();

    TraditionalOrder(OrderId id) {
        this.id = id;
    }

    private void requireStatus(Status expected) {
        if (status != expected)
            throw new DomainException("Invalid order status");
    }

    void addLine(ProductId p, Money unitPrice, Quantity q) {
        requireStatus(Status.DRAFT);
        lines.merge(p, new OrderLine(p, unitPrice, q),
                OrderLine::combine);
    }

    void place() {
        requireStatus(Status.DRAFT);
        if (lines.isEmpty())
            throw new DomainException("Cannot place empty order");
        status = Status.PLACED;
    }

    void pay() {
        requireStatus(Status.PLACED);
        status = Status.PAID;
    }

    void ship(TrackingNumber t, Instant at) {
        requireStatus(Status.PAID);
        shipment = Optional.of(new Shipment(t, at));
        status = Status.SHIPPED;
    }

    Money total() {
        return lines.values().stream()
                .map(OrderLine::total)
                .reduce(Money.zero(), Money::plus);
    }

    List<OrderLine> lines() {
        return List.copyOf(lines.values());
    }
}

enum Status {
    DRAFT, PLACED, PAID, SHIPPED, CANCELLED, DELIVERED
}

record OrderId(String value) {}
record ProductId(String value) {}
record TrackingNumber(String value) {}
record PaymentRef(String value) {}
record Reason(String value) {}
record Shipment(TrackingNumber tracking, Instant shippedAt) {}

record Quantity(int value) {
    Quantity {
        if (value <= 0)
            throw new DomainException("Quantity must be positive");
    }
}

record Money(double amount) {
    static Money zero() {
        return new Money(0);
    }

    Money plus(Money other) {
        return new Money(amount + other.amount);
    }
}

record OrderLine(ProductId product, Money unitPrice, Quantity quantity) {
    Money total() {
        return new Money(unitPrice.amount() * quantity.value());
    }

    OrderLine combine(OrderLine other) {
        return new OrderLine(product, unitPrice,
                new Quantity(quantity.value() + other.quantity.value()));
    }
}

class DomainException extends RuntimeException {
    DomainException(String message) {
        super(message);
    }
}
