public class ValidationThread extends Thread{
    String toBeValidated;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ValidationThread(String toBeValidated){
        this.toBeValidated = toBeValidated;
    }
    @Override
    public void run() {
        super.run();
    }
}
