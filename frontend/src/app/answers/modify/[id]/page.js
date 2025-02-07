"use client";

import { useParams, useRouter } from "next/navigation";
import { useEffect, useRef } from "react";
import SimpleMDE from "simplemde";
import "simplemde/dist/simplemde.min.css";

export default function QuestionModifyForm() {
  const params = useParams();
  const router = useRouter();
  const editorRef = useRef(null);
  const simpleMde = useRef(null);

  useEffect(() => {
    simpleMde.current = new SimpleMDE({
      element: editorRef.current,
      spellChecker: false,
      minHeight: "300px",
    });
    getOldData();
  }, []);

  const submitAnswer = (e) => {
    e.preventDefault();
    const updatedData = {
      content: editorRef.current.value,
      categoryId: params.id,
    };

    fetch("http://localhost:8080/api/v1/answers?id=" + params.id, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updatedData),
      credentials: "include",
    }).then((response) => {
      if (response.status === 200) {
        router.back();
      } else if (response.status === 401) {
        router.replace("/user/login");
      }
    });
  };

  const getOldData = async () => {
    try {
      const response = await fetch(
        "http://localhost:8080/api/v1/answers/" + params.id
      );
      if (!response.ok) {
        throw new Error("Failed to fetch");
      }
      const result = await response.json();
      simpleMde.current.value(result.data.content);
    } catch (error) {
      throw new Error("Error fetching data:", error);
    }
  };

  return (
    <div className="m-4">
      <h5>답변 수정</h5>
      <form onSubmit={submitAnswer}>
        <div className="mb-4">
          <label
            htmlFor="content"
            className="block text-sm font-medium text-gray-700"
          >
            내용
          </label>
          <textarea
            ref={editorRef}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          ></textarea>
        </div>

        <input
          type="submit"
          value="저장하기"
          className="w-full mt-4 px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
      </form>
    </div>
  );
}
