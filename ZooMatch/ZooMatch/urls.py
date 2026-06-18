import os

from django.conf.urls.static import static
from django.contrib import admin
from django.conf import settings
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('api.urls')),
] + static('media/users/', document_root=os.path.join(settings.MEDIA_ROOT,
                                                      'users')) \
  + static('media/pets/', document_root=os.path.join(settings.MEDIA_ROOT,
                                                     'pets')) \
  + static('media/comments/', document_root=os.path.join(settings.MEDIA_ROOT,
                                                         'comments')) \
  + static('media/chats/', document_root=os.path.join(settings.MEDIA_ROOT,
                                                      'chats'))

if settings.DEBUG:
    import debug_toolbar
    urlpatterns += [
        path('__debug__/', include(debug_toolbar.urls)),
    ]
