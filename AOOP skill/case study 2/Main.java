import java.time.Instant;

public class Main {
    public static void main(String[] args) {

        TraditionalOrder order =
                new TraditionalOrder(new OrderId("ORD101"));

        ProductId product = new ProductId("P101");

        order.addLine(
                product,
                new Money(500),
                new Quantity(2)
        );

        System.out.println("Total: " + order.total());

        order.place();
        order.pay();

        order.ship(
                new TrackingNumber("TRACK123"),
                Instant.now()
        );

        System.out.println("Order Status: SHIPPED");
    }
}