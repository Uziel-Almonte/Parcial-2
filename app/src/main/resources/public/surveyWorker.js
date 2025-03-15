self.addEventListener("message", async (event) => {
    if (event.data === "syncSurveys") {
        const localSurveys = await getLocalSurveys();
        let successCount = 0;
        
        for (const survey of localSurveys) {
            try {
                const response = await fetch("/api/surveys", { = await fetch("/api/surveys", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",  "Content-Type": "application/json",
                        "Authorization": `Bearer ${localStorage.getItem("token")}` ${localStorage.getItem("token")}`
                    }, },
                    body: JSON.stringify(survey.data)                    body: JSON.stringify(survey.data)
                });

                if (response.ok) {
                    await removeLocalSurvey(survey.id);t removeLocalSurvey(survey.id);
                    successCount++;
                    postMessage({ status: "success", survey: survey.id });   postMessage({ status: "success", survey: survey.id });
                }
            } catch (error) {
                console.error("Error syncing survey:", error);   console.error("Error syncing survey:", error);
            }   }
        }   }
             
        postMessage({        postMessage({
            status: "complete",
            uploaded: successCount,
            remaining: localSurveys.length - successCountlSurveys.length - successCount
        });
    }
});

async function getLocalSurveys() {async function getLocalSurveys() {
    const db = new Dexie("surveyDB");
    db.version(12).stores({
        surveys: "++id,name,sector,educationLevel,registeredBy,latitude,longitude,timestamp",sector,educationLevel,registeredBy,latitude,longitude,timestamp"
    });
    return db.surveys.toArray();urn db.surveys.toArray();
}








}    await db.surveys.delete(id);    });        surveys: "++id,name,sector,educationLevel,registeredBy,latitude,longitude,timestamp"    db.version(12).stores({


}    await db.surveys.delete(id);
    });
        surveys: "++id,name,sector,educationLevel,registeredBy,latitude,longitude,timestamp"    db.version(12).stores({    const db = new Dexie("surveyDB");async function removeLocalSurvey(id) {async function removeLocalSurvey(id) {
    const db = new Dexie("surveyDB");