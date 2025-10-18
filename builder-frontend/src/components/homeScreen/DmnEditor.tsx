import { onMount } from "solid-js";
import * as DmnEditor from "@kogito-tooling/kie-editors-standalone/dist/dmn";

import { saveDmn } from "@/api/dmn";


const DmnEd = () => {
  let container: HTMLDivElement | null = null;
  let editor: any = null;

  function buildDmnResources() {
    return new Map();
  }

  const initializeEditor = async () => {
    const initialDmn = "";

    editor = DmnEditor.open({
      container: container,
      initialContent: Promise.resolve(initialDmn),
      resources: buildDmnResources(),
      readOnly: false,
    });
  };

  const handleSave = async () => {
    if (editor) {
      const dmnContent = await editor.getContent();
      console.log("DMN Content:", dmnContent);
      saveDmn(dmnContent);
    }
  }

  onMount(async () => { initializeEditor(); });

  return (
    <>
      <div class="flex space-x-4 p-4 border-b-2 border-gray-200">
        <div onClick={handleSave} class="btn-default btn-blue">Save DMN</div>
      </div>

      <div class="h-[600px] overflow-auto">
        <div class="h-[600px]" ref={(el) => (container = el)} />
      </div>
    </>
  );
}
export default DmnEd;
