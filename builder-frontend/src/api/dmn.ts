import { authFetch } from "@/api/auth";


const apiUrl = import.meta.env.VITE_API_URL;

export const saveDmn = async (dmnModel) => {
  const requestData: any = { dmnModel: dmnModel };

  const url = apiUrl + "/dmn";
  try {
    const response = await authFetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(requestData),
    });

    if (!response.ok) {
      throw new Error(`Save failed with status: ${response.status}`);
    }
  } catch (error) {
    console.error("Error saving DMN:", error);
    throw error; // rethrow so you can handle it in your component if needed
  }
};