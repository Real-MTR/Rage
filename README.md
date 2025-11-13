# Rage
_A simple yet powerful packet-based sidebar api_

## How to use it?
First of all, create an adapter

```java
public class MyAdapter implements RageAdapter {
    @Override
    public String getTitle() {
        return "My Sidebar";
    }

    @Override
    public List<String> getLines() {
        return Arrays.asList("Line 1", "Line 2", "Line 3");
    }
}
```

Then, create an instance of the plugin and pass the adapter to it

```java
Rage rage = new Rage(this, new MyAdapter());

// This is optional, you can run an update task that will update the scoreboard
// Every 20 ticks (You can change it to whatever you want as long as it's not lower than 0)
rage.runTask(20);
```