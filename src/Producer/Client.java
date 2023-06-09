package Producer;

import Consumer.CashBoxType;

import java.util.Random;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class Client {
  int productsNumber;
  String type;
  private static Random random;

  public Client() {
    this.productsNumber = random.nextInt(15)+1;
    this.type = String.valueOf(productsNumber < 5 ? CashBoxType.FAST : CashBoxType.STANDARD);
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
    return random;
  }

  public int generateTimeToNext() {
    return random.nextInt(10) + 1;
  }
}
