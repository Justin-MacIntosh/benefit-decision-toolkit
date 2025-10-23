import { authFetch } from "@/api/auth";


function gzipString(str: string): Promise<ArrayBuffer> {
  const byteArray = new TextEncoder().encode(str);
  const cs = new CompressionStream("gzip");
  const writer = cs.writable.getWriter();
  writer.write(byteArray);
  writer.close();
  return new Response(cs.readable).arrayBuffer();
}

const apiUrl = import.meta.env.VITE_API_URL;

export const saveDmn = async (dmnModelXml: string) => {
  const compressedDmn: ArrayBuffer = await gzipString(dmnModelXml);
  console.log("Compressed DMN size (bytes):", compressedDmn.byteLength);

  const url = apiUrl + "/dmn";
  try {
    const response = await authFetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/octet-stream",
      },
      body: compressedDmn,
    });

    if (!response.ok) {
      throw new Error(`Save failed with status: ${response.status}`);
    }
  } catch (error) {
    console.error("Error saving DMN:", error);
    throw error; // rethrow so you can handle it in your component if needed
  }
};