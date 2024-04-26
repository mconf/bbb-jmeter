// post write websocket message

import groovy.json.*;

def request = sampler.getRequestData();
def request_json = new JsonSlurper().parseText(request);

if (request_json.type == "connection_init") {
  def msg = [:];
  msg.end_time = prev.getEndTime();
  vars.put("connection_init", JsonOutput.toJson(msg));
  return;
}

def id = request_json.id;

def msg = [:];
msg.id = id;
msg.end_time = prev.getEndTime();

def matcher = (request_json.payload.query =~ /^(?ms)(?<kind>\w+) (?<type>\w+).*/)
if (matcher.matches()) {
  def kind = matcher.group("kind");
  def type = matcher.group("type");

  msg.kind = kind;
  msg.type = type;

  // log.info("Message id " + id + ", kind " + kind + ", type " + type);
} else {
  // log.info("No match for query " + request_json.payload.query);
}

prev.setIgnore();
def msg_out = JsonOutput.toJson(msg);
// log.info("Registered " + msg_out);
vars.put(id, msg_out);
vars.put("write_next_id", (Long.valueOf(id) + 1).toString());
