## Documentation Swagger poert via docker 8081
    ** http://localhost:8080/api/swagger-ui.html

# Pour importer ou exporter les données de la BD du conteneur :

---
## 1. **Créer un dump de ta base PostgreSQL**
Sur ta machine (remplace `hospital_db` par le nom de ton conteneur db si besoin) :
```bash
docker exec -t hospital_db pg_dump -U bassilekin hospital_flask > dump.sql
```
- `hospital_db` : nom du conteneur PostgreSQL (vérifie avec `docker ps`)
- `bassilekin` : utilisateur PostgreSQL
- `hospital_flask` : nom de ta base de données
- `dump.sql` : fichier SQL généré sur ta machine
---
## 2. **Transférer le fichier `dump.sql`**

 Copie ce fichier sur la machine de destination (par clé USB, scp, etc.).
 
---
## 3. **Restaurer le dump sur la nouvelle machine**
Place `dump.sql` dans le dossier de ton projet, puis lance :
```bash
cat dump.sql | docker exec -i hospital_db psql -U bassilekin -d hospital_flask
```
- Cela injecte le contenu du dump dans la base PostgreSQL du conteneur.
---


## PatientController

### 1. Créer un patient
**POST** `/patients`  
**Body (JSON):**
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "numUrgence": 123456,
  "telephone": 987654321,
  "email": "jean.dupont@email.com",
  "groupeSanguin": "A+",
  "stade": "STADE_I",
  "symptomesManifester": ["fièvre", "toux"],
  "traitementSuivie": ["paracétamol"],
  "maladieIds": [1, 2]
}
```
**Réponse:** 201 Created + PatientDTO

---

### 2. Récupérer un patient par ID
**GET** `/patients/{id}`  
**Réponse:** 200 OK + PatientDTO

---

### 3. Modifier totalement un patient
**PUT** `/patients/{id}`  
**Body (JSON):** (identique à POST)  
**Réponse:** 200 OK + PatientDTO

---

### 4. Supprimer un patient
**DELETE** `/patients/{id}`  
**Réponse:** 204 No Content

---

### 5. Récupérer tous les patients
**GET** `/patients`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 6. Recherche par email ou téléphone
**GET** `/patients/search?email=...&telephone=...`  
**Exemple:** `/patients/search?email=jean.dupont@email.com`  
**Réponse:** 200 OK + PatientDTO

---

### 7. Filtrer par nom
**GET** `/patients/by-name?nom=Dupont`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 8. Filtrer par stade médical
**GET** `/patients/by-stade?stade=STADE_I`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 9. Filtrer par traitement
**GET** `/patients/by-traitement?traitement=paracétamol`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 10. Filtrer combiné (nom, stade, traitement)
**GET** `/patients/filter?nom=Dupont&stade=STADE_I&traitement=paracétamol`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 11. Statistiques patients
**GET** `/patients/stats`  
**Réponse:** 200 OK + `{...}`

---

### 12. Mise à jour partielle
**PATCH** `/patients/{id}`  
**Body (JSON):**
```json
{
  "stade": "STADE_II",
  "traitementSuivie": ["paracétamol", "ibuprofène"]
}
```
**Réponse:** 200 OK + PatientDTO

---

### 13. Maladies associées à un patient
**GET** `/patients/{patientId}/maladies`  
**Réponse:** 200 OK + `[MaladieDTO]`

---

## MaladiesController

### 1. Créer une maladie
**POST** `/maladies`  
**Body (JSON):**
```json
{
  "nom": "Grippe",
  "type": "Virale",
  "symptomes": ["fièvre", "toux"],
  "traitements": ["repos", "paracétamol"]
}
```
**Réponse:** 201 Created + MaladieDTO

---

### 2. Récupérer une maladie par ID
**GET** `/maladies/{id}`  
**Réponse:** 200 OK + MaladieDTO

---

### 3. Modifier une maladie
**PUT** `/maladies/{id}`  
**Body (JSON):** (identique à POST)  
**Réponse:** 200 OK + MaladieDTO

---

### 4. Suppression
**DELETE** `/maladies/{id}`  
**Réponse:** 204 No Content

---

### 5. Liste, filtrage par type ou nom
**GET** `/maladies?type=Virale&nom=Grippe`  
**Réponse:** 200 OK + `[MaladieDTO]`

---

### 6. Patients atteints d’une maladie
**GET** `/maladies/{maladieId}/patients`  
**Réponse:** 200 OK + `[PatientDTO]`

---

### 7. Recherche par nom
**GET** `/maladies/{name}/maladies`  
**Réponse:** 200 OK + MaladieDTO

---

### 8. Mise à jour partielle
**PATCH** `/maladies/{id}`  
**Body (JSON):**
```json
{
  "traitements": ["repos", "paracétamol", "vitamine C"]
}
```
**Réponse:** 200 OK + MaladieDTO

---

### 9. Statistiques maladies
**GET** `/maladies/stats`  
**Réponse:** 200 OK + `{...}`

---

## ImageController

### 1. Upload d’image
**POST** `/images/upload`  
**Body (form-data):**
- file: (fichier image)
- maladieId: 1

---

### 2. Récupérer une image
**GET** `/images/{id}`  
**Réponse:** 200 OK (binaire, type MIME selon image)

---

### 3. Supprimer une image
**DELETE** `/images/{id}`  
**Réponse:** 200 OK + message

---

**Remarque** :  
Pour chaque endpoint, configurez dans Postman l’URL, la méthode HTTP, les paramètres et le body comme indiqué ci-dessus.
