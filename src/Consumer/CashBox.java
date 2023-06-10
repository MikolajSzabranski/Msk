package Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class CashBox extends AtomicReference<CashBox> {
  public static int TIME_TO_NEXT = 1;
  int queueLen;
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
    queueLen = 0;
    random = new Random();
    this.type = type;
    this.maxLength = type == CashBoxType.STANDARD ? 15 : 10;
    this.speed = type == CashBoxType.STANDARD ? 30000 : 10000;
  }

  public int getQueueLen() {
    return queueLen;
  }

  public void incQueue() {
    queueLen++;
    if (queueLen > maxLength){
      System.out.println(("Kolejka w jednej z kas typu " + type + "jest za długa"));
      throw new RuntimeException();
    }
  }

  public int consume() {
    TIME_TO_NEXT = generateTimeToNext();
    int count = random.nextInt(15) + 1;
    System.out.println("I want to consume " + count + ". Next I'll be consuming in " + TIME_TO_NEXT);
    return count;
  }

  public int getTimeToNext() {
    return TIME_TO_NEXT;
  }

  private int generateTimeToNext() {
    return random.nextInt(10) + 1;
  }
}
