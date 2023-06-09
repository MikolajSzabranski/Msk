package Consumer;

import Producer.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class CashBox extends AtomicReference<CashBox> {
  int timeToNext;
  int queue;
  int speed;
  CashBoxType type;
  int maxLength;
  private Random random;

  public int getMaxLength() {
    return maxLength;
  }

  public static List<CashBox> FASTS = new ArrayList<>();
  public static List<CashBox> STANDARDS = new ArrayList<>();

  public CashBox(CashBoxType type) {
    queue = 0;
    random = new Random();
    timeToNext = generateTimeToNext();
    this.type = type;
    this.maxLength = type == CashBoxType.STANDARD ? 15 : 10;
    this.speed = type == CashBoxType.STANDARD ? 30000 : 10000;
  }

  public int getQueue() {
    return queue;
  }

  public void incQueue() {
    queue++;
    if (queue > maxLength){
      System.out.println(("Kolejka w jednej z kas typu " + type + "jest za długa"));
      throw new RuntimeException();
    }
  }

  public int consume() {
    timeToNext = generateTimeToNext();
    int count = random.nextInt(15) + 1;
    System.out.println("I want to consume " + count + ". Next I'll be consuming in " + timeToNext);
    return count;
  }

  public int getTimeToNext() {
    return timeToNext;
  }

  private int generateTimeToNext() {
    return random.nextInt(10) + 1;
  }
}
