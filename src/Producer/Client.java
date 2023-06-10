package Producer;

import Consumer.CashBoxType;

import java.util.Random;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class Client {
  int productsNumber;
  String type;
  public static int NEXT_CLIENT_APPEAR;
  private static final Random RANDOM = new Random();

  public Client(double currentTime) {
    this.productsNumber = generateRandom();
    this.type = String.valueOf(productsNumber < 5 ? CashBoxType.FAST : CashBoxType.STANDARD);
    NEXT_CLIENT_APPEAR = (int) (generateRandom() + currentTime);
  }

  private int generateRandom() {
    return RANDOM.nextInt(15) + 1;
  }

  public int getProductsNumber() {
    return productsNumber;
  }

  public void setProductsNumber(int productsNumber) {
    this.productsNumber = productsNumber;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Random getRandom() {
    return RANDOM;
  }

  public int generateTimeToNext() {
    return RANDOM.nextInt(10) + 1;
  }
}
