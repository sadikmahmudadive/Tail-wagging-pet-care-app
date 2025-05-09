from flask import Flask, request, jsonify, g
from flask_cors import CORS
import sqlite3

DATABASE = "calendar.db"

app = Flask(__name__)
CORS(app)  # Enable CORS for all domains

def get_db():
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
        db.row_factory = sqlite3.Row
    return db

@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

def init_db():
    with app.app_context():
        db = get_db()
        db.execute("""
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT
            )
        """)
        db.commit()

# Initialize database
init_db()

@app.route("/api/events", methods=["GET"])
def get_events():
    date = request.args.get("date")
    if not date:
        return jsonify({"error": "Missing date parameter"}), 400
    db = get_db()
    cur = db.execute("SELECT * FROM events WHERE date = ?", (date,))
    events = [dict(row) for row in cur.fetchall()]
    return jsonify(events)

@app.route("/api/events", methods=["POST"])
def add_event():
    data = request.get_json()
    date = data.get("date")
    title = data.get("title")
    description = data.get("description", "")
    if not date or not title:
        return jsonify({"error": "Missing date or title"}), 400
    db = get_db()
    cur = db.execute(
        "INSERT INTO events (date, title, description) VALUES (?, ?, ?)",
        (date, title, description)
    )
    db.commit()
    event_id = cur.lastrowid
    return jsonify({"id": event_id, "date": date, "title": title, "description": description})

@app.route("/api/events/<int:event_id>", methods=["PUT"])
def update_event(event_id):
    data = request.get_json()
    date = data.get("date")
    title = data.get("title")
    description = data.get("description", "")
    db = get_db()
    cur = db.execute(
        "UPDATE events SET date = ?, title = ?, description = ? WHERE id = ?",
        (date, title, description, event_id)
    )
    db.commit()
    if cur.rowcount == 0:
        return jsonify({"error": "Event not found"}), 404
    return jsonify({"id": event_id, "date": date, "title": title, "description": description})

@app.route("/api/events/<int:event_id>", methods=["DELETE"])
def delete_event(event_id):
    db = get_db()
    cur = db.execute("DELETE FROM events WHERE id = ?", (event_id,))
    db.commit()
    if cur.rowcount == 0:
        return jsonify({"error": "Event not found"}), 404
    return jsonify({"message": "Event deleted"})

if __name__ == "__main__":
    app.run(debug=True)