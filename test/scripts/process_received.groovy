// post read websocket message

import groovy.json.*;

vars.put("is_ka", "false");

def response = prev.getResponseDataAsString();
if (response == null) {
  log.info("No response, returning");
  return;
}

def json = new JsonSlurper().parseText(response);

if (json.type == "connection_ack") {
  prev.setSampleLabel(ctx.getThreadGroup().getName() + " connection_ack");
  def msg = new JsonSlurper().parseText(vars.get("connection_init"));
  def elapsed = prev.getEndTime() - Long.valueOf(msg.end_time);
  prev.setLatency(elapsed);
  return;
}

if (json.type == "ka") {
  if (vars.get("authenticated") == null) {
    return;
  }

  def loop_counter_s = vars.get("loop_counter");
  if (loop_counter_s == null) {
    log.info("loop_counter at this point is null");
  }
  def counter = Long.valueOf(vars.get("loop_counter")) - 1;
  log.info("Current loop_counter: " + counter + " (" + ctx.getThreadGroup().getName() + " " + ctx.getThreadNum() + ")");
  vars.put("loop_counter", counter.toString());
  prev.setIgnore();
  vars.put("is_ka", "true");
  return;
}

def id = json.id;
if (id == null) {
  log.info("No id on message: " + response);
  return;
}

def msg = vars.get(id);
if (msg == null) {
  log.info("Cannot find write information for id " + id);
  return;
}

msg = new JsonSlurper().parseText(msg);
def elapsed = prev.getEndTime() - Long.valueOf(msg.end_time);
prev.setLatency(elapsed);

def seen_key = "seen " + id;
if (vars.get(seen_key) == null && ((msg.kind == "mutation" && json.type == "complete") || msg.kind != "mutation")) {
  prev.setSampleLabel(ctx.getThreadGroup().getName() + " read kind " + msg.kind + ", type " + msg.type);
  vars.put(seen_key, "yes");

  if (id == "3") {
    vars.put("authenticated", "true");
  }
} else {
  prev.setSampleLabel(ctx.getThreadGroup().getName() + " read kind " + msg.kind + ", type " + msg.type + " (ignored)");
  prev.setIgnore();
}
