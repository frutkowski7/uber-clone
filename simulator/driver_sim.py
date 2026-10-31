import time
import json
import requests
import _thread as thread
from websocket import create_connection

BASE_URL = "http://localhost:8080/api"
WS_URL = "ws://localhost:8080/ws-uber/websocket"

DRIVER_EMAIL = "driver@example.com"
DRIVER_PASSWORD = "driver"

CLIENT_EMAIL = "client@example.com"
CLIENT_PASSWORD = "client"

# Przykładowe punkty GPS trasy kierowcy po Warszawie
ROUTE_COORDINATES = [
    {"lat": 52.2297, "lng": 21.0122},
    {"lat": 52.2305, "lng": 21.0135},
    {"lat": 52.2315, "lng": 21.0150},
    {"lat": 52.2325, "lng": 21.0170},
]

# przepis na kierowcę
class ClientAgent:
    def __init__(self,email,password):
        self.email = email 
        self.password = password 
        self.jwt_token = None 

    def login (self):
           # logowanie klienta
           try:
               res = requests.post(f"{BASE_URL}/auth/login", json= {
                   "email": self.email,
                   "password": self.password 
               })
               if res.status_code == 200:
                   data = res.json() 
                   self.jwt_token = data.get("token")
                   return True 
               else:
                   return False 
           except Exception as e:
               print("Brak połączenia z serwerem")
               return False

    def new_ride(self, pLat, pLng, dLat, dLng):
        headers = {
                    "Authorization": f"Bearer {self.jwt_token}",
                    "Content-Type": "application/json"
                }
        payload = {
                            "pickupLatitude": pLat,
                            "pickupLongitude": pLng,
                            "dropoffLatitude": dLat,
                            "dropoffLongitude": dLng
                        }
        try:    
            res = requests.post(f"{BASE_URL}/ride/new", json=payload, headers=headers )
            if res.status_code == 200:
                data = res.json()
                # zwracam id w celu przekazania do akceptacji trasy przez kierowcę 
                return data.get("id")
            else:
                return False 
        except Exception as e:
            print("Brak połączenia z serwerem")
            return False
        
        


class DriverAgent:
     # Konstruktor klasy DriverAgent (co przekazuje do klasy)
    def __init__ (self, email, password):
          self.email = email
          self.password = password
          self.current_lat = None
          self.current_lng = None
          self.jwt_token = None
          self.driver_id = None 
          self.ws = None    
          

    def fetch_route_from_osrm(self, start_lat, start_lng, end_lat, end_lng):
        # OSRM przyjmuje współrzędne w kolejności: longitude, latitude
        url = f"http://router.project-osrm.org/route/v1/driving/{start_lng},{start_lat};{end_lng},{end_lat}?overview=full&geometries=geojson"
        try:
            response = requests.get(url)
            if response.status_code == 200:
                data = response.json()
                raw_coords = data['routes'][0]['geometry']['coordinates']
                # coordinates to list - sublists [lng, lat]
                # raw_coords - [lng, lat] 
                return [{"lat": coord[1], "lng": coord[0]} for coord in raw_coords]
            else:
                print(f"Błąd OSRM: {response.status_code}")
                return []
        except Exception as e:
            print(f"Błąd połączenia z OSRM: {e}")
            return []

    def login (self):
        # logowanie kierowcy
        try:
            res = requests.post(f"{BASE_URL}/auth/login", json= {
                "email": self.email,
                "password": self.password 
            })
            if res.status_code == 200:
                data = res.json() 
                self.jwt_token = data.get("token")
                self.driver_id = data.get("userId")
                return True 
            else:
                return False 
        except Exception as e:
            print("Brak połączenia z serwerem")
            return False
        
    def send_location(self, lat, lng):
        print(self.jwt_token)
        headers = {
            "Authorization": f"Bearer {self.jwt_token}",
            "Content-Type": "application/json"
        }
        payload = {"lat":lat, "lng":lng}

        try:
            res = requests.post(f"{BASE_URL}/driver/location/update", json=payload, headers=headers)
            if res.status_code == 200:
                print(f"[GPS] Wysyłanie pozycji: lat={lat}, lng={lng} -> [200 OK]")       
                self.current_lng = lng 
                self.current_lat = lat
        except Exception as e:
            print("Error while updating location")
            return False

        # metoda jazdy po danym route (symulowanie ruchu)

    def follow_route(self, route, step_delay=0.5):
        for point in route:
            self.current_lat = point["lat"]
            self.current_lng = point["lng"]
            self.send_location(self.current_lat, self.current_lng)
            time.sleep(step_delay)

    def accept_ride(self, ride_id):
        headers = {
                    "Authorization": f"Bearer {self.jwt_token}",
                    "Content-Type": "application/json"
                }
        try:
            res = requests.post(f"{BASE_URL}/ride/{ride_id}/accept", headers=headers)
            if res.status_code == 200:
                print(f"ride {ride_id} accepted succesfully")
                ride_data = res.json()
                pickup_lat = ride_data.get("pickupLatitude")
                pickup_lng = ride_data.get("pickupLongitude")

                print(" COORDINATES FROM SPRING ",ride_data.get("coordinates"))

                route_to_passenger = self.fetch_route_from_osrm(
                    self.current_lat, self.current_lng, pickup_lat, pickup_lng
                )
                print("ROUTE TO PASSENGER", route_to_passenger)
                self.follow_route(route_to_passenger)


                
            if res.status_code == 404:
                print(f"ride {ride_id} already accepted!")
        except Exception as e:
            print("Error while updating location", e)
            return False
    def connect_websocket(self):
        print("Łączenie z websocket")
        try:
            self.ws = create_connection(WS_URL)
            connect_frame = (
                "CONNECT\n"
                f"passcode:{self.jwt_token}\n"
                "accept-version:1.1,1.0\n"
                "heart-beat:10000,10000\n\n"
                "\x00"
            )
            self.ws.send(connect_frame)

            topic = f"/topic/driver/{self.driver_id}/offers"
            subscribe_frame = (
                "SUBSCRIBE\n"
                "id:sub-0\n"
                f"destination:{topic}\n\n"
                "\x00"
            )
            self.ws.send(subscribe_frame)
            print(f"[WS] Zasubskrybowano kanał: {topic}")

            thread.start_new_thread(self._listen_websocket, ())
        except Exception as e:
            print(f"Nie udało się połączyć z WebSocket")

    def _listen_websocket(self):
        while True:
            try:
                message = self.ws.recv()
                if "MESSAGE" in message:
                    print("\nOtrzymano nową ofertę przejazdu!")
                    body = message.split("\n\n")[1].rstrip("\x00")
                    payload = json.loads(body)
                    
                    ride_id = payload.get("rideId")
                    self.accept_ride(ride_id)
            except Exception:
                break

    def start_simulation(self):
        if not self.login():
            return

        self.connect_websocket()

        print("\nStart wysyłania pozycji GPS")
        index = 0
        while True:
            coord = ROUTE_COORDINATES[index % len(ROUTE_COORDINATES)]
            self.send_location(coord["lat"], coord["lng"])
            index += 1
            time.sleep(3)


if __name__ == "__main__":
    driver = DriverAgent(DRIVER_EMAIL, DRIVER_PASSWORD)
    client = ClientAgent(CLIENT_EMAIL, CLIENT_PASSWORD)

    print("--- TEST 1: CLIENT LOGIN ---")
    if client.login():
        ride_id = client.new_ride(52.2288, 21.0032, 52.2382, 21.0322)

    print("--- TEST 2: DRIVER LOGIN ---")
    if driver.login():
    
        print("\n--- TEST 3: GPS LOCATION ---")
        driver.send_location(52.2297, 21.0122)

        driver.accept_ride(ride_id)

        # Punkty startowy i docelowy w Warszawie:
        # Start: Pałac Kultury i Nauki (Emilii Plater)
        START_LAT, START_LNG = 52.2318, 21.0060

        # Cel: Rondo Daszyńskiego
        END_LAT, END_LNG = 52.2300, 20.9840

        #print("\n--- TEST 3: WEBSOCKET CONNECT ---")
        #driver.connect_websocket()
        
        # Czekamy 5 sekund, żeby sprawdzić czy połączenie WS się nie wykraczy
        import time
        time.sleep(5)
            



          