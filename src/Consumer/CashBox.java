package Consumer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Stanislaw on 08.05.2018.
 */
public class CashBox extends AtomicReference<CashBox> {
  public static int TIME_TO_NEXT = 1;
  int queueLen;
  int speed;
  public CashBoxType type;
  int maxLength;
  private static final Random RANDOM = new Random();

  public int getMaxLength() {
    return maxLength;
  }

  public static ArrayList<CashBox> FASTS = new ArrayList<>();
  public static ArrayList<CashBox> STANDARDS = new ArrayList<>();

  public CashBox(CashBoxType type) {
    this.queueLen = 0;
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
    int count = RANDOM.nextInt(15) + 1;
    System.out.println("I want to consume " + count + ". Next I'll be consuming in " + TIME_TO_NEXT);
    return count;
  }

  public int getTimeToNext() {
    return TIME_TO_NEXT;
  }

  private int generateTimeToNext() {
    return RANDOM.nextInt(10) + 1;
  }
}
