import os
from celery import Celery

os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'ZooMatch.settings')

app = Celery('ZooMatch')
app.config_from_object('django.conf:settings', namespace='CELERY')
app.autodiscover_tasks()
