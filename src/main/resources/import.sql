-- INSERT INTO public.user(id, name, password, email) VALUES ('bbe0ba50-a3e8-4f53-b10f-20459347c0f5', 'Andrei', '$2a$10$ADyWdcX.L7JAwjTBvB91NuCK7ZUlnMPWyMrYGUX5ww6F8vGWhKnYu', 'john.doe@example.com');

-- INSERT INTO folder(id, folder_name, folder_path, folder_pkl_path, created_at, user_id) VALUES ('aa584c34-7f26-423b-8d71-2bec547cf41d', 'manyFiles', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFilespkl', '2024-01-27 18:32:02.395', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5');

-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('54273fa4-5bfc-4020-a293-44ad209f28ca', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_2.png', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_2.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_2.png', 'Screenshot_2.png', '2024-01-28 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('135369c2-222f-42ea-949a-2489c284c584', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_3.png', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_3.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_3.png', 'Screenshot_3.png', '2024-01-25 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('51e11a1e-ba15-4601-bb2d-84e9f6b1a0b0', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_8.png', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_8.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_8.png', 'Screenshot_8.png', '2024-01-22 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('8021b971-e2a5-49dd-a3fd-5fae02a39c23', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_9.png', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_9.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/Screenshot_9.png', 'Screenshot_9.png', '2024-01-20 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('5a6ad84f-842b-40eb-9048-f421d4feba3f', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/15-Como-fotografar-pessoas-Fotografia-Dicas.jpg', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/15-Como-fotografar-pessoas-Fotografia-Dicas.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/15-Como-fotografar-pessoas-Fotografia-Dicas.jpg', '15-Como-fotografar-pessoas-Fotografia-Dicas.jpg', '2024-01-27 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
-- INSERT INTO folder_content(id, file_path, pkl_file_path, url, file_name, created_at, folder_id) VALUES('31edf38e-ebf5-4166-aace-8a0710a972e4', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/20181218nvidia_rostos_ai_2-680x601.jpg', 'bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/20181218nvidia_rostos_ai_2-680x601.pkl', 'https://balde-teste232323.s3.sa-east-1.amazonaws.com/bbe0ba50-a3e8-4f53-b10f-20459347c0f5/manyFiles/20181218nvidia_rostos_ai_2-680x601.jpg', '20181218nvidia_rostos_ai_2-680x601.jpg', '2024-01-29 18:32:02.396', 'aa584c34-7f26-423b-8d71-2bec547cf41d');
