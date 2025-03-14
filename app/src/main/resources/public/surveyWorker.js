self.addEventListener("message", async (event) => {
    if (event.data === "syncSurveys") {
        const unsyncedSurveys = await getUnsyncedSurveys();
        for (const survey of unsyncedSurveys) {
            try {
                const response = await fetch("/api/surveys", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${localStorage.getItem("token")}`
                    },
                    body: JSON.stringify(survey)
                });

                if (response.ok) {
                    await markSurveyAsSynced(survey.id);
                    postMessage({ status: "success", survey });
                } else {
                    console.error("Failed to sync survey:", survey);
                }
            } catch (error) {
                console.error("Error syncing survey:", error);
            }
        }
    }
});

async function getUnsyncedSurveys() {
    const db = new Dexie("surveyDB");
    db.version(12).stores({
        surveys: "++id,name,sector,educationLevel,registeredBy,latitude,longitude,timestamp,synced"
    });
    return db.surveys.where("synced").equals(false).toArray();
}

async function markSurveyAsSynced(id) {
    const db = new Dexie("surveyDB");
    db.version(12).stores({
        surveys: "++id,name,sector,educationLevel,registeredBy,latitude,longitude,timestamp,synced"
    });
    await db.surveys.update(id, { synced: true });
}