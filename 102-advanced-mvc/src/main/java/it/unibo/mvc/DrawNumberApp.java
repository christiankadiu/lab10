
package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
*/
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *              the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) throws IOException {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view : views) {
            view.setObserver(this);
            view.start();
        }
        final Configuration.Builder builder = new Configuration.Builder();
        // this.model = new DrawNumberImpl(MIN, MAX, ATTEMPTS);

        try (InputStream is = ClassLoader.getSystemResourceAsStream("config.yml");
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) { // NOPMD
                final StringTokenizer st = new StringTokenizer(line);
                while (st.hasMoreTokens()) {
                    final String s = st.nextToken();
                    if (s.matches("minimum:")) {
                        builder.settMin(Integer.parseInt(st.nextToken()));
                    } else if (s.matches("maximum:")) {
                        builder.settMax(Integer.parseInt(st.nextToken()));
                    } else if (s.matches("attempts:")) {
                        builder.setAttempts(Integer.parseInt(st.nextToken()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // NOPMD
        }
        final Configuration configuration = builder.build();
        this.model = new DrawNumberImpl(configuration.getMin(), configuration.getMax(), configuration.getAttempts());
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view : views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view : views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException, IOException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(System.out),
                new PrintStreamView("output.txt"));
    }
}
