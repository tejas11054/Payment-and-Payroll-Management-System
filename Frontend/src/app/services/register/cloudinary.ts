import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Cloudinary {
  
  private http = inject(HttpClient);

  // === IMPORTANT: Yeh details apne Cloudinary account se daalein ===
  private cloudName = 'dkcme2wzg'; // Apna Cloud Name yahan daalein
  private uploadPreset = 'organisationRegisteration'; // Apna Unsigned Upload Preset yahan daalein
  // ===============================================================

  private cloudinaryUrl = `https://api.cloudinary.com/v1_1/${this.cloudName}/image/upload`;

  /**
   * Uploads a single file to Cloudinary.
   * @param file The file to upload.
   * @returns An Observable containing the secure URL of the uploaded file.
   */
  uploadFile(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', this.uploadPreset);

    return this.http.post<any>(this.cloudinaryUrl, formData).pipe(
      map((response: { secure_url: any; }) => response.secure_url) // Humein sirf file ka URL chahiye
    );
  }
}
